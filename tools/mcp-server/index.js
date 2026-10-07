#!/usr/bin/env node

/**
 * ServiceForge MCP Server
 *
 * Exposes MCP tools for prompt-driven job booking/dispatching.
 *
 * Transport: stdio (required by VS Code MCP).
 */

console.log('ServiceForge MCP server starting (stdio) - version: 20260925-2');

const axios = require('axios');
const {
  McpServer,
} = require('@modelcontextprotocol/sdk/server/mcp.js');
const { StdioServerTransport } = require('@modelcontextprotocol/sdk/server/stdio.js');
const { z } = require('zod');

const SF_BACKEND = process.env.SF_BACKEND_URL || 'http://localhost:8080';

// Optional: generated tools from OpenAPI (see tools/mcp-server/generate-tools-from-openapi.mjs)
let registerGeneratedTools = null;
try {
  // eslint-disable-next-line global-require
  ({ registerGeneratedTools } = require('./generated-tools'));
} catch {
  // No generated tools present; that's OK.
}

function pad2(n) {
  return String(n).padStart(2, '0');
}

function toLocalDateTimeStringFromParts(parts) {
  // parts: [YYYY,MM,DD,HH,mm,ss]
  const year = parts[0];
  const month = parts[1];
  const day = parts[2];
  const hour = parts[3] ?? 0;
  const minute = parts[4] ?? 0;
  const second = parts[5] ?? 0;

  // Use UTC arithmetic to avoid local timezone/DST shifts when adding minutes.
  const startEpochUtc = Date.UTC(year, month - 1, day, hour, minute, second);
  const d = new Date(startEpochUtc);
  return `${d.getUTCFullYear()}-${pad2(d.getUTCMonth() + 1)}-${pad2(d.getUTCDate())}T${pad2(d.getUTCHours())}:${pad2(d.getUTCMinutes())}:${pad2(d.getUTCSeconds())}`;
}

function addMinutesLocalDateTimeString(startLocalDateTime, minutesToAdd) {
  const parts = startLocalDateTime.split(/[-T:]/).map(Number);
  const year = parts[0];
  const month = parts[1];
  const day = parts[2];
  const hour = parts[3] || 0;
  const minute = parts[4] || 0;
  const second = parts[5] || 0;

  const startEpochUtc = Date.UTC(year, month - 1, day, hour, minute, second);
  const endEpochUtc = startEpochUtc + minutesToAdd * 60 * 1000;
  const endUtc = new Date(endEpochUtc);
  return `${endUtc.getUTCFullYear()}-${pad2(endUtc.getUTCMonth() + 1)}-${pad2(endUtc.getUTCDate())}T${pad2(endUtc.getUTCHours())}:${pad2(endUtc.getUTCMinutes())}:${pad2(endUtc.getUTCSeconds())}`;
}

function parseBookingPrompt(prompt) {
  const techMatch = prompt.match(/technician\s*(?:id\s*)?(\d+)/i);
  const technicianId = techMatch ? Number(techMatch[1]) : 1;

  const dateMatch = prompt.match(/(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(?::\d{2})?)/);
  if (!dateMatch) throw new Error('start time not found in prompt');
  let startTime = dateMatch[1];
  if (startTime.length === 16) startTime = `${startTime}:00`;

  const durMatch = prompt.match(/(\d+)\s*minutes?/i);
  const durationMinutes = durMatch ? Number(durMatch[1]) : 60;

  // Normalize startTime to YYYY-MM-DDTHH:mm:ss
  const startParts = startTime.split(/[-T:]/).map(Number);
  const startTimeStr = toLocalDateTimeStringFromParts(startParts);
  const endTimeStr = addMinutesLocalDateTimeString(startTimeStr, durationMinutes);

  const forParts = prompt.split(/for\s+/i);
  let customerName = 'Customer';
  if (forParts.length > 1) {
    customerName = forParts[forParts.length - 1].split(/[.,\n]/)[0].trim();
  }

  return { technicianId, customerName, startTime: startTimeStr, endTime: endTimeStr };
}

async function main() {
  const server = new McpServer({
    name: 'serviceforge-dispatch',
    version: '0.1.0',
  });

  function safeJsonStringify(value) {
    try {
      return JSON.stringify(value, null, 2);
    } catch {
      return String(value);
    }
  }

  async function sfGet(path) {
    return axios.get(`${SF_BACKEND}${path}`, { validateStatus: () => true });
  }

  async function sfPost(path, config) {
    return axios.post(`${SF_BACKEND}${path}`, undefined, {
      ...config,
      validateStatus: () => true,
    });
  }

  async function sfDelete(path) {
    return axios.delete(`${SF_BACKEND}${path}`, { validateStatus: () => true });
  }

  server.tool(
    'book_job',
    'Book a job for a technician (creates a scheduled job).',
    {
      technicianId: z.number().int().positive().describe('Technician ID'),
      customerName: z.string().min(1).describe('Customer name'),
      startTime: z.string().min(19).describe('LocalDateTime string: YYYY-MM-DDTHH:mm:ss'),
      endTime: z.string().min(19).describe('LocalDateTime string: YYYY-MM-DDTHH:mm:ss'),
    },
    async ({ technicianId, customerName, startTime, endTime }) => {
      const payload = { technicianId, customerName, startTime, endTime };
      const resp = await axios.post(`${SF_BACKEND}/api/jobs`, payload, {
        headers: { 'Content-Type': 'application/json' },
        validateStatus: () => true,
      });

      if (resp.status >= 200 && resp.status < 300) {
        return {
          content: [
            { type: 'text', text: JSON.stringify(resp.data, null, 2) },
          ],
        };
      }

      return {
        isError: true,
        content: [
          {
            type: 'text',
            text: `Backend error (${resp.status}): ${JSON.stringify(resp.data)}`,
          },
        ],
      };
    }
  );

  server.tool(
    'book_job_from_prompt',
    'Parse a natural-language prompt and book a job.',
    {
      prompt: z.string().min(1).describe('Prompt like: "book job for technician 1 at 2026-09-25T14:00 for 60 minutes for Acme"'),
    },
    async ({ prompt }) => {
      const parsed = parseBookingPrompt(prompt);
      const resp = await axios.post(`${SF_BACKEND}/api/jobs`, parsed, {
        headers: { 'Content-Type': 'application/json' },
        validateStatus: () => true,
      });

      if (resp.status >= 200 && resp.status < 300) {
        return {
          content: [
            { type: 'text', text: `Parsed:\n${JSON.stringify(parsed, null, 2)}\n\nCreated:\n${JSON.stringify(resp.data, null, 2)}` },
          ],
        };
      }

      return {
        isError: true,
        content: [
          {
            type: 'text',
            text: `Parsed:\n${JSON.stringify(parsed, null, 2)}\n\nBackend error (${resp.status}): ${JSON.stringify(resp.data)}`,
          },
        ],
      };
    }
  );

  server.tool(
    'list_technicians',
    'List technicians from ServiceForge backend.',
    {},
    async () => {
      const resp = await sfGet('/api/technicians');
      if (resp.status >= 200 && resp.status < 300) {
        return { content: [{ type: 'text', text: JSON.stringify(resp.data, null, 2) }] };
      }
      return { isError: true, content: [{ type: 'text', text: `Backend error (${resp.status}): ${JSON.stringify(resp.data)}` }] };
    }
  );

  // -----------------------------
  // Parts reservation MCP tools
  // -----------------------------

  server.tool(
    'list_part_reservations',
    'List part reservations from ServiceForge backend.',
    {},
    async () => {
      const resp = await sfGet('/api/parts');
      if (resp.status >= 200 && resp.status < 300) {
        return { content: [{ type: 'text', text: safeJsonStringify(resp.data) }] };
      }
      return { isError: true, content: [{ type: 'text', text: `Backend error (${resp.status}): ${safeJsonStringify(resp.data)}` }] };
    }
  );

  server.tool(
    'list_part_orders',
    'List part orders (if any) from ServiceForge backend.',
    {},
    async () => {
      const resp = await sfGet('/api/parts/orders');
      if (resp.status >= 200 && resp.status < 300) {
        return { content: [{ type: 'text', text: safeJsonStringify(resp.data) }] };
      }
      return { isError: true, content: [{ type: 'text', text: `Backend error (${resp.status}): ${safeJsonStringify(resp.data)}` }] };
    }
  );

  server.tool(
    'reserve_parts',
    'Reserve parts for a job (creates a part reservation).',
    {
      sku: z.string().min(1).describe('Part SKU (e.g., PART-001)'),
      quantity: z.number().int().positive().describe('Quantity to reserve'),
      jobId: z.number().int().positive().describe('Job ID'),
      technicianId: z.number().int().positive().optional().describe('Technician ID (optional)'),
    },
    async ({ sku, quantity, jobId, technicianId }) => {
      const params = new URLSearchParams();
      params.set('sku', sku);
      params.set('quantity', String(quantity));
      params.set('jobId', String(jobId));
      if (technicianId !== undefined && technicianId !== null) {
        params.set('technicianId', String(technicianId));
      }

      const resp = await sfPost(`/api/parts/reservations?${params.toString()}`, {
        headers: { 'Content-Type': 'application/json' },
      });

      if (resp.status >= 200 && resp.status < 300) {
        return { content: [{ type: 'text', text: safeJsonStringify(resp.data) }] };
      }

      return {
        isError: true,
        content: [{ type: 'text', text: `Backend error (${resp.status}): ${safeJsonStringify(resp.data)}` }],
      };
    }
  );

  server.tool(
    'cancel_part_reservation',
    'Cancel (release) a part reservation by id.',
    {
      reservationId: z.number().int().positive().describe('Reservation ID'),
    },
    async ({ reservationId }) => {
      const resp = await sfDelete(`/api/parts/reservations/${reservationId}`);
      if (resp.status >= 200 && resp.status < 300) {
        return { content: [{ type: 'text', text: `Cancelled reservation ${reservationId}` }] };
      }
      return { isError: true, content: [{ type: 'text', text: `Backend error (${resp.status}): ${safeJsonStringify(resp.data)}` }] };
    }
  );

  server.tool(
    'restock_part',
    'Restock a part SKU by quantity.',
    {
      sku: z.string().min(1).describe('Part SKU'),
      quantity: z.number().int().positive().describe('Quantity to add to inventory'),
    },
    async ({ sku, quantity }) => {
      const resp = await sfPost(`/api/parts/${encodeURIComponent(sku)}/restock?quantity=${encodeURIComponent(String(quantity))}`, {
        headers: { 'Content-Type': 'application/json' },
      });
      if (resp.status >= 200 && resp.status < 300) {
        return { content: [{ type: 'text', text: `Restocked ${sku} by ${quantity}` }] };
      }
      return { isError: true, content: [{ type: 'text', text: `Backend error (${resp.status}): ${safeJsonStringify(resp.data)}` }] };
    }
  );

  // -----------------------------
  // Generated MCP tools (OpenAPI)
  // -----------------------------
  if (typeof registerGeneratedTools === 'function') {
    registerGeneratedTools(server, {
      SF_BACKEND,
      safeJsonStringify,
      sfRequest: async (method, url, options) => {
        // axios expects full URL; url already includes SF_BACKEND
        return axios({ method, url, validateStatus: () => true, ...options });
      },
    });
  }

  const transport = new StdioServerTransport();
  await server.connect(transport);
}

main().catch((err) => {
  // stderr is what VS Code MCP logs as server stderr
  console.error('Fatal MCP server error:', err);
  process.exit(1);
});

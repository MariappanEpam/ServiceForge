import { createReadStream } from 'node:fs';
import { stat } from 'node:fs/promises';
import { basename } from 'node:path';
import fetch from 'node-fetch';
import { z } from 'zod';

import {
  McpServer,
} from '@modelcontextprotocol/sdk/server/mcp.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';

const BASE_URL = process.env.OPENPROJECT_BASE_URL || 'http://localhost:8088';
const API_KEY = process.env.OPENPROJECT_API_KEY;
const PROJECT_IDENTIFIER = process.env.OPENPROJECT_PROJECT_IDENTIFIER || 'serviceforge';

if (!API_KEY) {
  // MCP servers should fail fast if required config is missing.
  console.error('OPENPROJECT_API_KEY is required. Create an OpenProject API token and set it in your environment.');
  process.exit(2);
}

function authHeaders(extra = {}) {
  const basic = Buffer.from(`apikey:${API_KEY}`, 'utf8').toString('base64');
  return {
    Authorization: `Basic ${basic}`,
    Accept: 'application/hal+json',
    ...extra,
  };
}

async function httpJson(method, path, body) {
  const url = `${BASE_URL}${path}`;
  const res = await fetch(url, {
    method,
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: body ? JSON.stringify(body) : undefined,
  });

  const text = await res.text();
  let json;
  try {
    json = text ? JSON.parse(text) : null;
  } catch {
    json = { raw: text };
  }

  if (!res.ok) {
    throw new Error(`${method} ${path} failed: ${res.status} ${res.statusText}\n${typeof json === 'object' ? JSON.stringify(json, null, 2) : String(json)}`);
  }

  return json;
}

async function getProject() {
  return httpJson('GET', `/api/v3/projects/${encodeURIComponent(PROJECT_IDENTIFIER)}`);
}

async function listWorkPackages({ projectHref, subjectContains, typeHref, statusHref, pageSize = 100 }) {
  const filters = [];
  if (projectHref) filters.push({ project: { operator: '=', values: [projectHref] } });
  if (typeHref) filters.push({ type: { operator: '=', values: [typeHref] } });
  if (statusHref) filters.push({ status: { operator: '=', values: [statusHref] } });
  if (subjectContains) filters.push({ subject: { operator: '~', values: [subjectContains] } });

  const qp = new URLSearchParams();
  if (filters.length) qp.set('filters', JSON.stringify(filters));
  qp.set('pageSize', String(pageSize));

  return httpJson('GET', `/api/v3/work_packages?${qp.toString()}`);
}

function findBySubject(collection, subject) {
  const els = collection?._embedded?.elements || [];
  return els.find((e) => e.subject === subject);
}

async function createWorkPackage({ projectHref, typeHref, subject, descriptionRaw }) {
  const payload = {
    subject,
    description: { raw: descriptionRaw || '' },
    _links: {
      project: { href: projectHref },
      type: { href: typeHref },
    },
  };
  return httpJson('POST', '/api/v3/work_packages', payload);
}

async function updateWorkPackageDescription({ workPackageHref, descriptionRaw }) {
  // OpenProject uses a lockVersion for updates.
  const current = await httpJson('GET', workPackageHref);
  const lockVersion = current?.lockVersion;
  if (typeof lockVersion !== 'number') throw new Error('Could not read lockVersion for work package');

  const payload = {
    lockVersion,
    description: { raw: descriptionRaw || '' },
  };

  return httpJson('PATCH', workPackageHref, payload);
}

async function addComment({ workPackageHref, commentRaw }) {
  // Comments are activities; create a work package comment via activities endpoint.
  // OpenProject supports POST /work_packages/:id/activities with { comment: { raw } }
  const payload = { comment: { raw: commentRaw || '' } };
  return httpJson('POST', `${workPackageHref}/activities`, payload);
}

async function uploadAttachment({ filePath, fileName, description }) {
  const s = await stat(filePath);
  const name = fileName || basename(filePath);

  const url = `${BASE_URL}/api/v3/attachments`;
  const res = await fetch(url, {
    method: 'POST',
    headers: authHeaders({
      'Content-Type': 'application/octet-stream',
      'Content-Length': String(s.size),
      'Content-Disposition': `attachment; filename="${name.replaceAll('"', '')}"`,
      ...(description ? { 'X-Description': description } : {}),
    }),
    body: createReadStream(filePath),
  });

  const text = await res.text();
  let json;
  try {
    json = text ? JSON.parse(text) : null;
  } catch {
    json = { raw: text };
  }

  if (!res.ok) {
    throw new Error(`POST /api/v3/attachments failed: ${res.status} ${res.statusText}\n${typeof json === 'object' ? JSON.stringify(json, null, 2) : String(json)}`);
  }

  return json;
}

async function attachFileToWorkPackage({ workPackageHref, attachmentHref }) {
  // Update work package with _links.attachments
  const current = await httpJson('GET', workPackageHref);
  const lockVersion = current?.lockVersion;
  if (typeof lockVersion !== 'number') throw new Error('Could not read lockVersion for work package');

  const existing = current?._links?.attachments || [];
  const next = [...existing, { href: attachmentHref }];

  const payload = {
    lockVersion,
    _links: {
      attachments: next,
    },
  };

  return httpJson('PATCH', workPackageHref, payload);
}

const server = new McpServer({
  name: 'serviceforge-openproject-mcp',
  version: '0.1.0',
});

server.tool(
  'openproject_get_project',
  {
    title: 'Get OpenProject project',
    description: 'Get the OpenProject project by identifier (default: serviceforge).',
    inputSchema: z.object({
      projectIdentifier: z.string().optional(),
    }),
  },
  async ({ projectIdentifier }) => {
    if (projectIdentifier && projectIdentifier !== PROJECT_IDENTIFIER) {
      // allow override per call
      const proj = await httpJson('GET', `/api/v3/projects/${encodeURIComponent(projectIdentifier)}`);
      return { content: [{ type: 'text', text: JSON.stringify(proj, null, 2) }] };
    }
    const proj = await getProject();
    return { content: [{ type: 'text', text: JSON.stringify(proj, null, 2) }] };
  }
);

server.tool(
  'openproject_list_work_packages',
  {
    title: 'List work packages',
    description: 'List work packages with optional filters (project/type/status/subject).',
    inputSchema: z.object({
      projectHref: z.string().optional(),
      subjectContains: z.string().optional(),
      typeHref: z.string().optional(),
      statusHref: z.string().optional(),
      pageSize: z.number().int().min(1).max(500).optional(),
    }),
  },
  async (args) => {
    const projectHref = args.projectHref || (await getProject())?._links?.self?.href;
    const list = await listWorkPackages({ ...args, projectHref });
    return { content: [{ type: 'text', text: JSON.stringify(list, null, 2) }] };
  }
);

server.tool(
  'openproject_ensure_work_package',
  {
    title: 'Ensure work package',
    description: 'Ensure a work package exists by exact subject; create if missing.',
    inputSchema: z.object({
      subject: z.string().min(1),
      descriptionRaw: z.string().optional(),
      // Default types: Task=1, Summary task=3
      typeHref: z.string().default('/api/v3/types/1'),
      projectHref: z.string().optional(),
    }),
  },
  async ({ subject, descriptionRaw, typeHref, projectHref }) => {
    const projHref = projectHref || (await getProject())?._links?.self?.href;
    const existing = await listWorkPackages({ projectHref: projHref, pageSize: 200 });
    const found = findBySubject(existing, subject);
    if (found) {
      return {
        content: [{ type: 'text', text: JSON.stringify({ action: 'exists', workPackage: found }, null, 2) }],
      };
    }
    const created = await createWorkPackage({ projectHref: projHref, typeHref, subject, descriptionRaw });
    return {
      content: [{ type: 'text', text: JSON.stringify({ action: 'created', workPackage: created }, null, 2) }],
    };
  }
);

server.tool(
  'openproject_update_work_package_description',
  {
    title: 'Update work package description',
    description: 'Replace a work package description (raw markdown/text).',
    inputSchema: z.object({
      workPackageHref: z.string().min(1),
      descriptionRaw: z.string().optional(),
    }),
  },
  async ({ workPackageHref, descriptionRaw }) => {
    const updated = await updateWorkPackageDescription({ workPackageHref, descriptionRaw });
    return { content: [{ type: 'text', text: JSON.stringify(updated, null, 2) }] };
  }
);

server.tool(
  'openproject_add_comment',
  {
    title: 'Add work package comment',
    description: 'Add a comment to a work package activity stream.',
    inputSchema: z.object({
      workPackageHref: z.string().min(1),
      commentRaw: z.string().min(1),
    }),
  },
  async ({ workPackageHref, commentRaw }) => {
    const created = await addComment({ workPackageHref, commentRaw });
    return { content: [{ type: 'text', text: JSON.stringify(created, null, 2) }] };
  }
);

server.tool(
  'openproject_attach_file',
  {
    title: 'Attach file to work package',
    description: 'Upload a local file and attach it to a work package.',
    inputSchema: z.object({
      workPackageHref: z.string().min(1),
      filePath: z.string().min(1),
      fileName: z.string().optional(),
      description: z.string().optional(),
    }),
  },
  async ({ workPackageHref, filePath, fileName, description }) => {
    const attachment = await uploadAttachment({ filePath, fileName, description });
    const attachmentHref = attachment?._links?.self?.href;
    if (!attachmentHref) {
      return { content: [{ type: 'text', text: JSON.stringify({ attachment, attached: false }, null, 2) }] };
    }
    const updated = await attachFileToWorkPackage({ workPackageHref, attachmentHref });
    return { content: [{ type: 'text', text: JSON.stringify({ attachment, attached: true, workPackage: updated }, null, 2) }] };
  }
);

const transport = new StdioServerTransport();
await server.connect(transport);

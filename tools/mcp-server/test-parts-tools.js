#!/usr/bin/env node

/**
 * Minimal test runner for ServiceForge MCP parts tools.
 *
 * This does NOT start an MCP stdio session; it validates the tool behavior by
 * calling the same backend endpoints the MCP tools call.
 *
 * Usage:
 *   node test-parts-tools.js
 *
 * Env:
 *   SF_BACKEND_URL=http://localhost:8080
 */

const assert = require('assert');
const axios = require('axios');

const SF_BACKEND = process.env.SF_BACKEND_URL || 'http://localhost:8080';

async function sfGet(path) {
  return axios.get(`${SF_BACKEND}${path}`, { validateStatus: () => true });
}

async function sfPost(path) {
  return axios.post(`${SF_BACKEND}${path}`, undefined, { validateStatus: () => true });
}

async function sfDelete(path) {
  return axios.delete(`${SF_BACKEND}${path}`, { validateStatus: () => true });
}

async function main() {
  // 1) Ensure backend is reachable
  {
    const resp = await sfGet('/api/technicians');
    assert(resp.status >= 200 && resp.status < 300, `Backend not reachable: /api/technicians -> ${resp.status}`);
  }

  // 2) Create a job to attach reservations to
  // Use a time window far in the future to avoid collisions with previous test runs.
  let jobId;
  {
    const baseEpoch = Date.now();
    const start = new Date(baseEpoch + 365 * 24 * 60 * 60 * 1000); // ~1 year ahead
    start.setUTCMinutes(0, 0, 0);
    const end = new Date(start.getTime() + 60 * 60 * 1000);

    const startTime = start.toISOString().slice(0, 19);
    const endTime = end.toISOString().slice(0, 19);

    const payload = {
      technicianId: 1,
      customerName: 'MCP Parts Test',
      startTime,
      endTime,
    };

    const resp = await axios.post(`${SF_BACKEND}/api/jobs`, payload, {
      headers: { 'Content-Type': 'application/json' },
      validateStatus: () => true,
    });

    assert(resp.status >= 200 && resp.status < 300, `Job create failed: ${resp.status} ${JSON.stringify(resp.data)}`);
    jobId = resp.data?.id;
    assert(Number.isInteger(jobId) || typeof jobId === 'number', `Job id missing in response: ${JSON.stringify(resp.data)}`);
  }

  // 3) Restock a SKU so reserve succeeds
  const sku = 'SKU-1000';
  {
    const resp = await sfPost(`/api/parts/${encodeURIComponent(sku)}/restock?quantity=2`);
    assert(resp.status >= 200 && resp.status < 300, `Restock failed: ${resp.status} ${JSON.stringify(resp.data)}`);
  }

  // 4) Reserve parts
  let reservationId;
  {
    const resp = await sfPost(`/api/parts/reservations?sku=${encodeURIComponent(sku)}&quantity=1&jobId=${encodeURIComponent(String(jobId))}&technicianId=1`);
    assert(resp.status >= 200 && resp.status < 300, `Reserve failed: ${resp.status} ${JSON.stringify(resp.data)}`);

    const r = resp.data;
    reservationId = r?.id;
    assert(reservationId !== undefined && reservationId !== null, `Reservation id missing: ${JSON.stringify(r)}`);
    assert(String(r?.sku) === sku, `Reservation sku mismatch: expected ${sku}, got ${r?.sku}`);
    assert(Number(r?.quantity) === 1 || Number(r?.requestedQuantity) === 1, `Reservation quantity mismatch: ${JSON.stringify(r)}`);
  }

  // 5) List reservations and validate it contains the reservation
  {
    const resp = await sfGet('/api/parts');
    assert(resp.status >= 200 && resp.status < 300, `List reservations failed: ${resp.status} ${JSON.stringify(resp.data)}`);

    const list = resp.data;
    assert(Array.isArray(list), `Expected array from /api/parts, got: ${typeof list}`);
    const found = list.find((x) => String(x?.id) === String(reservationId));
    assert(found, `Reservation ${reservationId} not found in list`);
  }

  // 6) Cancel reservation
  {
    const resp = await sfDelete(`/api/parts/reservations/${encodeURIComponent(String(reservationId))}`);
    assert(resp.status === 204 || (resp.status >= 200 && resp.status < 300), `Cancel failed: ${resp.status} ${JSON.stringify(resp.data)}`);
  }

  // 7) Verify reservation removed
  {
    const resp = await sfGet('/api/parts');
    assert(resp.status >= 200 && resp.status < 300, `List reservations failed: ${resp.status} ${JSON.stringify(resp.data)}`);

    const list = resp.data;
    const found = list.find((x) => String(x?.id) === String(reservationId));
    assert(!found, `Reservation ${reservationId} still present after cancel`);
  }

  console.log('OK: parts tools backend validation passed');
}

main().catch((err) => {
  console.error('FAILED:', err?.stack || err);
  process.exit(1);
});

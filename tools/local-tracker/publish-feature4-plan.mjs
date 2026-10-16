import fs from 'node:fs';
import fetch from 'node-fetch';

const base = process.env.OPENPROJECT_BASE_URL || 'http://localhost:8088';
const proj = process.env.OPENPROJECT_PROJECT_IDENTIFIER || 'serviceforge';
const key = process.env.OPENPROJECT_API_KEY;

if (!key) {
  throw new Error('OPENPROJECT_API_KEY not set');
}

const auth = 'Basic ' + Buffer.from('apikey:' + key).toString('base64');

async function j(url, opts = {}) {
  const r = await fetch(url, {
    ...opts,
    headers: {
      Authorization: auth,
      'Content-Type': 'application/json',
      ...(opts.headers || {}),
    },
  });
  const t = await r.text();
  let data;
  try {
    data = JSON.parse(t);
  } catch {
    data = t;
  }
  return { status: r.status, data };
}

// get project id
const pr = await j(`${base}/api/v3/projects/${proj}`);
if (pr.status >= 300) throw new Error(`project lookup failed ${pr.status} ${JSON.stringify(pr.data)}`);
const projectId = Number(String(pr.data._links.self.href).split('/').pop());

// list types
const types = await j(`${base}/api/v3/types`);
if (types.status >= 300) throw new Error(`types failed ${types.status} ${JSON.stringify(types.data)}`);
const all = types.data._embedded?.elements || [];

function pick(name) {
  const n = String(name).toLowerCase();
  return all.find((t) => String(t.name).toLowerCase() === n) || all.find((t) => String(t.name).toLowerCase().includes(n));
}

const typeTask = pick('task') || all[0];
if (!typeTask) throw new Error('no types');

const title = 'Implementation Plan: Feature 4 — Parts Reservation';

// search existing by subject
const filters = [
  { subject: { operator: '~', values: [title] } },
  { project: { operator: '=', values: [String(projectId)] } },
];

const wp = await j(`${base}/api/v3/work_packages?filters=${encodeURIComponent(JSON.stringify(filters))}`);
if (wp.status >= 300) throw new Error(`wp search failed ${wp.status} ${JSON.stringify(wp.data)}`);

let existing = (wp.data._embedded?.elements || []).find((e) => e.subject === title);
let wpHref;

if (existing) {
  wpHref = existing._links.self.href;
} else {
  const payload = {
    subject: title,
    _links: {
      project: { href: `/api/v3/projects/${projectId}` },
      type: { href: typeTask._links.self.href },
    },
  };

  const created = await j(`${base}/api/v3/work_packages`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });

  if (created.status >= 300) throw new Error(`create failed ${created.status} ${JSON.stringify(created.data)}`);
  wpHref = created.data._links.self.href;
}

// update description
const plan = fs.readFileSync('tools/local-tracker/_feature4_plan.md', 'utf8');

const getWp = await j(`${base}${wpHref}`);
if (getWp.status >= 300) throw new Error(`get wp failed ${getWp.status} ${JSON.stringify(getWp.data)}`);

const lock = getWp.data.lockVersion;
const patch = { lockVersion: lock, description: { raw: plan } };

const upd = await j(`${base}${wpHref}`, {
  method: 'PATCH',
  body: JSON.stringify(patch),
});

if (upd.status >= 300) throw new Error(`update failed ${upd.status} ${JSON.stringify(upd.data)}`);

console.log(`UPDATED ${base}${wpHref}`);

import fetch from 'node-fetch';

const BASE_URL = process.env.OPENPROJECT_BASE_URL || 'http://localhost:8088';
const API_KEY = process.env.OPENPROJECT_API_KEY;
const PROJECT_IDENTIFIER = process.env.OPENPROJECT_PROJECT_IDENTIFIER || 'serviceforge';

if (!API_KEY) {
  console.error('Missing OPENPROJECT_API_KEY env var. Create an API token in OpenProject and set it in your terminal session.');
  process.exit(2);
}

function authHeaders() {
  // OpenProject API v3 supports API key via Basic auth: username=apikey, password=<token>
  const basic = Buffer.from(`apikey:${API_KEY}`, 'utf8').toString('base64');
  return {
    Authorization: `Basic ${basic}`,
    'Content-Type': 'application/json',
    Accept: 'application/hal+json'
  };
}

async function http(method, path, body) {
  const url = `${BASE_URL}${path}`;
  const res = await fetch(url, {
    method,
    headers: authHeaders(),
    body: body ? JSON.stringify(body) : undefined
  });

  const text = await res.text();
  let json;
  try {
    json = text ? JSON.parse(text) : null;
  } catch {
    json = { raw: text };
  }

  if (!res.ok) {
    const msg = typeof json === 'object' ? JSON.stringify(json, null, 2) : String(json);
    throw new Error(`${method} ${path} failed: ${res.status} ${res.statusText}\n${msg}`);
  }

  return json;
}

async function getProject() {
  return http('GET', `/api/v3/projects/${encodeURIComponent(PROJECT_IDENTIFIER)}`);
}

async function listWorkPackages(projectHref) {
  // Filter by project (OpenProject expects project ID values, not hrefs)
  const projectId = String(projectHref).split('/').filter(Boolean).pop();
  const qp = new URLSearchParams({
    filters: JSON.stringify([{ project: { operator: '=', values: [projectId] } }]),
  });
  return http('GET', `/api/v3/work_packages?${qp.toString()}`);
}

function findBySubject(collection, subject) {
  const els = collection?._embedded?.elements || [];
  return els.find((e) => e.subject === subject);
}

async function createWorkPackage(projectHref, typeHref, subject, description) {
  const payload = {
    subject,
    description: { raw: description },
    _links: {
      project: { href: projectHref },
      type: { href: typeHref }
    }
  };
  return http('POST', '/api/v3/work_packages', payload);
}

async function ensureWorkPackage(projectHref, typeHref, subject, description) {
  const existing = await listWorkPackages(projectHref);
  const found = findBySubject(existing, subject);
  if (found) {
    return { action: 'exists', wp: found };
  }
  const created = await createWorkPackage(projectHref, typeHref, subject, description);
  return { action: 'created', wp: created };
}

async function main() {
  const project = await getProject();
  const projectHref = project?._links?.self?.href;
  if (!projectHref) throw new Error('Could not resolve project href');

  // Default type ids in OpenProject: Task=1, Summary task=3 (as seen in UI URLs)
  const typeTaskHref = '/api/v3/types/1';
  const typeSummaryHref = '/api/v3/types/3';

  const epicSubject = 'EPIC: ServiceForge Delivery (Features 2 & 3)';
  const epicDesc = [
    'Tracks delivery of:',
    '- Feature 2: Parts Reservation',
    '- Feature 3: Technician Onboarding & Management',
    '',
    'Artifacts live in repo under pipeline/ (spec, architecture, review, implementation plan, tests).'
  ].join('\n');

  const f2Subject = 'Story: Feature 2 — Parts Reservation';
  const f2Desc = [
    'Repo artifacts:',
    '- Spec: pipeline/features/feature-2-parts-reservation.md',
    '- Architecture: pipeline/architecture/feature-2-architecture.md',
    '- Review: pipeline/reviews/feature-2-review.md',
    '- Implementation plan: pipeline/implementation-plan/feature-2-implementation-plan.md',
    '',
    'Definition of Done: implemented + tests passing per pipeline/orchestration.md guardrails.'
  ].join('\n');

  const f3Subject = 'Story: Feature 3 — Technician Onboarding & Management';
  const f3Desc = [
    'Repo artifacts:',
    '- Spec: pipeline/features/feature-3-technician-onboarding-management.md',
    '- Architecture: pipeline/architecture/feature-3-architecture.md',
    '- Review: pipeline/reviews/feature-3-review.md',
    '- Implementation plan: pipeline/implementation-plan/feature-3-implementation-plan.md',
    '- Handoff: pipeline/handoffs/feature-3-handoff.md (and UPDATE)',
    '',
    'Definition of Done: implemented + tests passing per pipeline/orchestration.md guardrails.'
  ].join('\n');

  const results = [];
  results.push(await ensureWorkPackage(projectHref, typeSummaryHref, epicSubject, epicDesc));
  results.push(await ensureWorkPackage(projectHref, typeTaskHref, f2Subject, f2Desc));
  results.push(await ensureWorkPackage(projectHref, typeTaskHref, f3Subject, f3Desc));

  for (const r of results) {
    const id = r.wp?.id ?? r.wp?.raw?.id;
    const href = r.wp?._links?.self?.href;
    console.log(`${r.action.toUpperCase()}: ${r.wp.subject} (id=${id ?? 'n/a'}) ${href ? `${BASE_URL}${href}` : ''}`);
  }
}

main().catch((err) => {
  console.error(err?.stack || String(err));
  process.exit(1);
});

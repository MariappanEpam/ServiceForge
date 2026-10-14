export type ApiError = {
  timestamp?: string
  status: number
  error?: string
  message: string
  path?: string
  code?: string
}

export class HttpError extends Error {
  status: number
  body?: unknown

  constructor(message: string, status: number, body?: unknown) {
    super(message)
    this.name = 'HttpError'
    this.status = status
    this.body = body
  }
}

async function parseJsonSafe(res: Response): Promise<unknown | undefined> {
  const contentType = res.headers.get('content-type') || ''
  if (!contentType.includes('application/json')) return undefined
  try {
    return await res.json()
  } catch {
    return undefined
  }
}

export async function httpGet<T>(path: string): Promise<T> {
  const res = await fetch(path, {
    method: 'GET',
    headers: {
      Accept: 'application/json',
    },
  })

  if (!res.ok) {
    const body = await parseJsonSafe(res)
    const msg = (body as ApiError | undefined)?.message || res.statusText
    throw new HttpError(msg, res.status, body)
  }

  return (await res.json()) as T
}

export async function httpPost<TResponse, TBody>(path: string, body: TBody): Promise<TResponse> {
  const res = await fetch(path, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  })

  if (!res.ok) {
    const parsed = await parseJsonSafe(res)
    const msg = (parsed as ApiError | undefined)?.message || res.statusText
    throw new HttpError(msg, res.status, parsed)
  }

  return (await res.json()) as TResponse
}

export async function httpPut<TResponse, TBody>(path: string, body: TBody): Promise<TResponse> {
  const res = await fetch(path, {
    method: 'PUT',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  })

  if (!res.ok) {
    const parsed = await parseJsonSafe(res)
    const msg = (parsed as ApiError | undefined)?.message || res.statusText
    throw new HttpError(msg, res.status, parsed)
  }

  return (await res.json()) as TResponse
}

export async function httpDelete(path: string): Promise<void> {
  const res = await fetch(path, {
    method: 'DELETE',
    headers: {
      Accept: 'application/json',
    },
  })

  if (!res.ok) {
    const parsed = await parseJsonSafe(res)
    const msg = (parsed as ApiError | undefined)?.message || res.statusText
    throw new HttpError(msg, res.status, parsed)
  }
}

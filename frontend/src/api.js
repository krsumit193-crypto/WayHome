export async function searchStations(q) {
  const res = await fetch(`/api/stations?q=${encodeURIComponent(q)}`);
  return res.ok ? res.json() : [];
}

export async function searchRoutes(body) {
  const res = await fetch('/api/routes/search', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || 'Search failed. Is the backend running?');
  return data;
}

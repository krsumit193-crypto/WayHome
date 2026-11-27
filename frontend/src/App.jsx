import { useMemo, useState } from 'react';
import StationInput, { parseCode } from './StationInput.jsx';
import RouteCard from './RouteCard.jsx';
import { searchRoutes } from './api.js';

const CLASSES = [
  ['ANY', 'Any class'],
  ['SL', 'Sleeper'],
  ['AC3', 'AC 3 Tier'],
  ['AC2', 'AC 2 Tier'],
  ['GEN', 'General'],
];

const SORTS = [
  ['FASTEST', 'Fastest', (a, b) => a.durationMinutes - b.durationMinutes],
  ['CHEAPEST', 'Cheapest', (a, b) => a.estimatedFare - b.estimatedFare],
  ['FEWEST_CHANGES', 'Fewest changes', (a, b) => a.changes - b.changes || a.durationMinutes - b.durationMinutes],
  ['SAFEST', 'Safest transfers', (a, b) => riskRank(a) - riskRank(b) || a.durationMinutes - b.durationMinutes],
];

const riskRank = (j) => ['LOW', 'MEDIUM', 'HIGH'].indexOf(j.risk);

function tomorrow() {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return d.toISOString().slice(0, 10);
}

export default function App() {
  const [from, setFrom] = useState('Jaipur Jn (JP)');
  const [to, setTo] = useState('Koderma Jn (KQR)');
  const [date, setDate] = useState(tomorrow());
  const [travelClass, setTravelClass] = useState('ANY');
  const [maxChanges, setMaxChanges] = useState(2);
  const [sort, setSort] = useState('FASTEST');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const routes = useMemo(() => {
    if (!result) return [];
    const sorter = SORTS.find((s) => s[0] === sort)[2];
    return [...result.routes].sort(sorter);
  }, [result, sort]);

  async function onSubmit(e) {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      setResult(await searchRoutes({ from: parseCode(from), to: parseCode(to), date, travelClass, maxChanges }));
    } catch (err) {
      setResult(null);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function swap() {
    setFrom(to);
    setTo(from);
  }

  return (
    <>
      <header className="board">
        <span className="board-name">RAILHOP</span>
        <span className="board-sub">No direct train? Find a way.</span>
      </header>

      <main className="wrap">
        <form className="panel" onSubmit={onSubmit}>
          <div className="row-two">
            <StationInput label="From" value={from} onChange={setFrom} placeholder="Station name or code" />
            <button type="button" className="swap" onClick={swap} aria-label="Swap stations">
              ⇄
            </button>
            <StationInput label="To" value={to} onChange={setTo} placeholder="Station name or code" />
          </div>
          <div className="row-three">
            <label className="field">
              <span>Journey date</span>
              <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
            </label>
            <label className="field">
              <span>Class</span>
              <select value={travelClass} onChange={(e) => setTravelClass(e.target.value)}>
                {CLASSES.map(([v, l]) => (
                  <option key={v} value={v}>{l}</option>
                ))}
              </select>
            </label>
            <div className="field">
              <span>Max train changes</span>
              <div className="seg">
                {[1, 2, 3].map((n) => (
                  <button type="button" key={n} className={n === maxChanges ? 'on' : ''} onClick={() => setMaxChanges(n)}>
                    {n}
                  </button>
                ))}
              </div>
            </div>
          </div>
          <button className="go" disabled={loading}>{loading ? 'Searching…' : 'Find my route'}</button>
        </form>

        {error && <p className="notice error">{error}</p>}

        {result && (
          <section>
            <div className="sortbar">
              {SORTS.map(([v, label]) => (
                <button key={v} className={v === sort ? 'on' : ''} onClick={() => setSort(v)}>{label}</button>
              ))}
            </div>
            {routes.length === 0 ? (
              <p className="notice">No route found. Try more changes or another date.</p>
            ) : (
              routes.map((j, i) => <RouteCard key={i} journey={j} />)
            )}
            <p className="muted disclaimer">{result.disclaimer}</p>
          </section>
        )}
      </main>
    </>
  );
}

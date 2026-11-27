import { useEffect, useId, useState } from 'react';
import { searchStations } from './api.js';

// "Jaipur Jn (JP)" -> "JP"; plain "jp" -> "JP"
export function parseCode(value) {
  const m = value.match(/\(([A-Za-z0-9]+)\)\s*$/);
  return (m ? m[1] : value.trim()).toUpperCase();
}

export default function StationInput({ label, value, onChange, placeholder }) {
  const listId = useId();
  const [options, setOptions] = useState([]);

  useEffect(() => {
    const q = value.replace(/\(.*\)\s*$/, '').trim();
    if (q.length < 2) {
      setOptions([]);
      return;
    }
    const timer = setTimeout(() => {
      searchStations(q).then(setOptions).catch(() => setOptions([]));
    }, 200);
    return () => clearTimeout(timer);
  }, [value]);

  return (
    <label className="field">
      <span>{label}</span>
      <input
        list={listId}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        autoComplete="off"
        required
      />
      <datalist id={listId}>
        {options.map((o) => (
          <option key={o.code} value={`${o.name} (${o.code})`} />
        ))}
      </datalist>
    </label>
  );
}

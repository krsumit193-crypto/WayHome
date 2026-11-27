import { Fragment } from 'react';

const LEG_COLORS = ['var(--blue)', 'var(--red)', 'var(--green)', 'var(--amber)'];
const RISK_LABEL = { LOW: 'Comfortable transfers', MEDIUM: 'Tight transfers', HIGH: 'Risky transfers' };

function duration(minutes) {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return h ? `${h}h ${m}m` : `${m}m`;
}

function clock(time, dayOffset) {
  return dayOffset > 0 ? `${time} +${dayOffset}d` : time;
}

export default function RouteCard({ journey }) {
  const { legs } = journey;
  return (
    <article className="ticket">
      <header className="ticket-head">
        <div>
          <strong className="big">{duration(journey.durationMinutes)}</strong>
          <span className="muted">
            {journey.changes === 0 ? 'Direct' : `${journey.changes} change${journey.changes > 1 ? 's' : ''}`}
          </span>
        </div>
        <div className="fare">
          <strong className="big">≈ ₹{journey.estimatedFare}</strong>
          <span className="muted">estimated, not a booking price</span>
        </div>
      </header>

      <ol className="track">
        <li className="stop">
          <time>{clock(legs[0].departure, legs[0].departureDay)}</time>
          <span>{legs[0].fromName}</span>
        </li>
        {legs.map((leg, i) => {
          const last = i === legs.length - 1;
          const wait = journey.transferWaitMinutes[i];
          return (
            <Fragment key={`${leg.trainNumber}-${i}`}>
              <li className="leg" style={{ '--c': LEG_COLORS[i % LEG_COLORS.length] }}>
                <div>
                  <strong>{leg.trainNumber}</strong> {leg.trainName}
                  <div className="muted">
                    {leg.distanceKm} km · ≈ ₹{leg.estimatedFare}
                  </div>
                </div>
                <a className="book" href={leg.bookingUrl} target="_blank" rel="noreferrer">
                  Check on IRCTC
                </a>
              </li>
              <li className={last ? 'stop' : 'stop interchange'}>
                <time>
                  {clock(leg.arrival, leg.arrivalDay)}
                  {!last && ` → ${clock(legs[i + 1].departure, legs[i + 1].departureDay)}`}
                </time>
                <span>
                  {leg.toName}
                  {!last && <em className="chip">wait {duration(wait)}</em>}
                </span>
              </li>
            </Fragment>
          );
        })}
      </ol>

      <footer className="ticket-foot">
        <span className={`risk risk-${journey.risk.toLowerCase()}`}>
          {journey.changes === 0 ? 'No transfers' : RISK_LABEL[journey.risk]}
        </span>
        {journey.overnightWait && <span className="risk risk-high">Overnight wait</span>}
      </footer>
    </article>
  );
}

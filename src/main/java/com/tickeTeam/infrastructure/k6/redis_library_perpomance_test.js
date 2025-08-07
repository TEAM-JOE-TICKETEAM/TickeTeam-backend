import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 500,
  duration: '30s',
};

export default function () {
  const res = http.post('http://localhost:8080/api/seats/select', JSON.stringify({
    seatId: '123'
  }), {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, {
    'is status 200 or 409': (r) => r.status === 200 || r.status === 409,
  });

  sleep(1);
}

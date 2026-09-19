// 5 coin packages, $1 - $100. First purchase for any account is auto x2 (spec: "بونص أول شحنة x2").
export const COIN_PACKAGES = [
  { id: 'p1', usd: 1, coins: 1000 },
  { id: 'p2', usd: 5, coins: 6000 },
  { id: 'p3', usd: 10, coins: 13000 },
  { id: 'p4', usd: 25, coins: 35000 },
  { id: 'p5', usd: 100, coins: 150000 },
];

export function getPackage(id) {
  return COIN_PACKAGES.find(p => p.id === id);
}

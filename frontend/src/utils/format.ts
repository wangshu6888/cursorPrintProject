/**
 * Format a value into a readable time string.
 * Handles array format [y, m, d, h, mm] and ISO string format.
 */
export function formatTime(v: unknown): string {
  if (!v) return ''
  if (Array.isArray(v) && v.length >= 5) {
    const [y, m, d, hh, mm] = v as number[]
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(hh).padStart(2, '0')}:${String(mm).padStart(2, '0')}`
  }
  const s = String(v)
  if (s.includes('T')) {
    return s.replace('T', ' ').substring(0, 16)
  }
  return s
}

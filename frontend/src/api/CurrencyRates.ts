export interface CurrencyConversionRates {
  result: string
  documentation: string
  terms_of_use: string
  time_last_update_unix: number
  time_last_update_utc: string
  time_next_update_unix: number
  time_next_update_utc: string
  base_code: string
  conversion_rates: Record<string, number>
}

export async function fetchCurrencyRates(): Promise<CurrencyConversionRates> {
  const response = await fetch('/api/currency', {
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('Failed to load currency rates')
  }

  return response.json()
}

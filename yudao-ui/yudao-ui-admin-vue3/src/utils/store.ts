const STORE_ID_KEY = 'hotel-store-id'

/**
 * 当前门店视角：空 / '0' 表示集团视角（不过滤）。
 */
export const getStoreId = (): string => {
  return localStorage.getItem(STORE_ID_KEY) || ''
}

export const setStoreId = (storeId: number | string | null | undefined) => {
  if (storeId === null || storeId === undefined || storeId === '' || String(storeId) === '0') {
    localStorage.removeItem(STORE_ID_KEY)
    return
  }
  localStorage.setItem(STORE_ID_KEY, String(storeId))
}

export const clearStoreId = () => localStorage.removeItem(STORE_ID_KEY)

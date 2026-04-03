import { supabase } from './supabase'

const BUCKET = 'photos'

export interface Bin {
  id: string
  user_id: string
  label: string
  photo_url: string
  created_at: string
  updated_at: string
}

export interface Item {
  id: string
  user_id: string
  label: string
  photo_url: string
  bin_id: string | null
  created_at: string
  updated_at: string
}

// Bins

export async function fetchBins(): Promise<Bin[]> {
  const { data, error } = await supabase
    .from('bins')
    .select('*')
    .order('created_at', { ascending: false })
  if (error) throw error
  return data
}

export async function createBin(label: string, photoFile: File): Promise<Bin> {
  const { data: { user } } = await supabase.auth.getUser()
  if (!user) throw new Error('Not authenticated')

  const binId = crypto.randomUUID()
  const path = `${user.id}/bins/${binId}.jpg`

  const { error: uploadError } = await supabase.storage
    .from(BUCKET)
    .upload(path, photoFile, { contentType: photoFile.type, upsert: true })
  if (uploadError) throw uploadError

  const { data: { publicUrl } } = supabase.storage.from(BUCKET).getPublicUrl(path)

  const { data, error } = await supabase
    .from('bins')
    .insert({ label, photo_url: publicUrl })
    .select()
    .single()
  if (error) throw error
  return data
}

export async function updateBin(id: string, fields: { label?: string; photo_url?: string }): Promise<Bin> {
  const { data, error } = await supabase
    .from('bins')
    .update(fields)
    .eq('id', id)
    .select()
    .single()
  if (error) throw error
  return data
}

export async function deleteBin(id: string, photoUrl: string): Promise<void> {
  const path = storagePathFromUrl(photoUrl)
  if (path) {
    await supabase.storage.from(BUCKET).remove([path])
  }
  const { error } = await supabase.from('bins').delete().eq('id', id)
  if (error) throw error
}

export async function countItemsInBin(binId: string): Promise<number> {
  const { count, error } = await supabase
    .from('items')
    .select('*', { head: true, count: 'exact' })
    .eq('bin_id', binId)
  if (error) throw error
  return count ?? 0
}

// Items

export async function fetchItems(): Promise<Item[]> {
  const { data, error } = await supabase
    .from('items')
    .select('*')
    .order('created_at', { ascending: false })
  if (error) throw error
  return data
}

export async function fetchItemsByBin(binId: string): Promise<Item[]> {
  const { data, error } = await supabase
    .from('items')
    .select('*')
    .eq('bin_id', binId)
    .order('created_at', { ascending: false })
  if (error) throw error
  return data
}

export async function createItem(label: string, photoFile: File, binId?: string): Promise<Item> {
  const { data: { user } } = await supabase.auth.getUser()
  if (!user) throw new Error('Not authenticated')

  const itemId = crypto.randomUUID()
  const path = `${user.id}/items/${itemId}.jpg`

  const { error: uploadError } = await supabase.storage
    .from(BUCKET)
    .upload(path, photoFile, { contentType: photoFile.type, upsert: true })
  if (uploadError) throw uploadError

  const { data: { publicUrl } } = supabase.storage.from(BUCKET).getPublicUrl(path)

  const { data, error } = await supabase
    .from('items')
    .insert({ label, photo_url: publicUrl, bin_id: binId ?? null })
    .select()
    .single()
  if (error) throw error
  return data
}

export async function updateItem(id: string, fields: Record<string, unknown>): Promise<Item> {
  const { data, error } = await supabase
    .from('items')
    .update(fields)
    .eq('id', id)
    .select()
    .single()
  if (error) throw error
  return data
}

export async function deleteItem(id: string, photoUrl: string): Promise<void> {
  const path = storagePathFromUrl(photoUrl)
  if (path) {
    await supabase.storage.from(BUCKET).remove([path])
  }
  const { error } = await supabase.from('items').delete().eq('id', id)
  if (error) throw error
}

export async function uploadPhoto(file: File, folder: 'items' | 'bins', entityId: string): Promise<string> {
  const { data: { user } } = await supabase.auth.getUser()
  if (!user) throw new Error('Not authenticated')

  const path = `${user.id}/${folder}/${entityId}.jpg`

  const { error } = await supabase.storage
    .from(BUCKET)
    .upload(path, file, { contentType: file.type, upsert: true })
  if (error) throw error

  const { data: { publicUrl } } = supabase.storage.from(BUCKET).getPublicUrl(path)
  return publicUrl
}

export async function deletePhoto(publicUrl: string): Promise<void> {
  const path = storagePathFromUrl(publicUrl)
  if (path) {
    await supabase.storage.from(BUCKET).remove([path])
  }
}

function storagePathFromUrl(publicUrl: string): string | null {
  const marker = `/${BUCKET}/`
  const idx = publicUrl.indexOf(marker)
  if (idx === -1) return null
  return publicUrl.slice(idx + marker.length)
}

import { useState, useEffect } from 'react'
import { FaXmark } from 'react-icons/fa6'
import PhotoDropzone from './PhotoDropzone'
import { createItem, fetchBins, type Bin } from '../../lib/storage-api'

interface CreateItemModalProps {
  preselectedBinId?: string
  onClose: () => void
  onCreated: () => void
}

export default function CreateItemModal({ preselectedBinId, onClose, onCreated }: CreateItemModalProps) {
  const [label, setLabel] = useState('')
  const [file, setFile] = useState<File | null>(null)
  const [binId, setBinId] = useState(preselectedBinId ?? '')
  const [bins, setBins] = useState<Bin[]>([])
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchBins().then(setBins).catch(() => {})
  }, [])

  async function handleSave() {
    if (!label.trim() || !file) return
    setSaving(true)
    setError('')
    try {
      await createItem(label.trim(), file, binId || undefined)
      onCreated()
      onClose()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to create item')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm" onClick={onClose}>
      <div
        className="bg-gray-900/90 border border-gray-800 rounded-2xl p-6 w-full max-w-md mx-4"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-100">New Item</h2>
          <button onClick={onClose} className="p-1 rounded-full hover:bg-gray-800 text-gray-400">
            <FaXmark className="w-4 h-4" />
          </button>
        </div>

        <div className="space-y-4">
          <PhotoDropzone file={file} onFileSelect={setFile} />

          <input
            type="text"
            placeholder="Item label"
            value={label}
            onChange={(e) => setLabel(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSave()}
            className="w-full px-4 py-2.5 bg-gray-800/60 border border-gray-700 rounded-xl text-gray-100 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-400/50"
          />

          <select
            value={binId}
            onChange={(e) => setBinId(e.target.value)}
            className="w-full px-4 py-2.5 bg-gray-800/60 border border-gray-700 rounded-xl text-gray-100 focus:outline-none focus:ring-2 focus:ring-purple-400/50"
          >
            <option value="">No bin</option>
            {bins.map((bin) => (
              <option key={bin.id} value={bin.id}>{bin.label}</option>
            ))}
          </select>

          {error && <p className="text-sm text-red-400">{error}</p>}

          <button
            onClick={handleSave}
            disabled={!label.trim() || !file || saving}
            className="w-full py-2.5 rounded-xl font-medium transition-colors bg-purple-500 hover:bg-purple-600 text-white disabled:bg-gray-700 disabled:text-gray-500"
          >
            {saving ? (
              <div className="w-5 h-5 mx-auto border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : 'Create Item'}
          </button>
        </div>
      </div>
    </div>
  )
}

import { useState } from 'react'
import { FaXmark } from 'react-icons/fa6'
import PhotoDropzone from './PhotoDropzone'
import { createBin } from '../../lib/storage-api'

interface CreateBinModalProps {
  onClose: () => void
  onCreated: () => void
}

export default function CreateBinModal({ onClose, onCreated }: CreateBinModalProps) {
  const [label, setLabel] = useState('')
  const [file, setFile] = useState<File | null>(null)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  async function handleSave() {
    if (!label.trim() || !file) return
    setSaving(true)
    setError('')
    try {
      await createBin(label.trim(), file)
      onCreated()
      onClose()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to create bin')
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
          <h2 className="text-lg font-semibold text-gray-100">New Bin</h2>
          <button onClick={onClose} className="p-1 rounded-full hover:bg-gray-800 text-gray-400">
            <FaXmark className="w-4 h-4" />
          </button>
        </div>

        <div className="space-y-4">
          <PhotoDropzone file={file} onFileSelect={setFile} />

          <input
            type="text"
            placeholder="Bin label"
            value={label}
            onChange={(e) => setLabel(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSave()}
            className="w-full px-4 py-2.5 bg-gray-800/60 border border-gray-700 rounded-xl text-gray-100 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-400/50"
          />

          {error && <p className="text-sm text-red-400">{error}</p>}

          <button
            onClick={handleSave}
            disabled={!label.trim() || !file || saving}
            className="w-full py-2.5 rounded-xl font-medium transition-colors bg-purple-500 hover:bg-purple-600 text-white disabled:bg-gray-700 disabled:text-gray-500"
          >
            {saving ? (
              <div className="w-5 h-5 mx-auto border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : 'Create Bin'}
          </button>
        </div>
      </div>
    </div>
  )
}

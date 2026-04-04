import { useState, useEffect } from 'react'
import { FaArrowLeft, FaTrash, FaLinkSlash } from 'react-icons/fa6'
import { fetchItemsByBin, deleteItem, updateItem, type Item, type Bin } from '../../lib/storage-api'
import ImageModal from './ImageModal'

interface BinDetailProps {
  bin: Bin
  onBack: () => void
}

export default function BinDetail({ bin, onBack }: BinDetailProps) {
  const [items, setItems] = useState<Item[]>([])
  const [loading, setLoading] = useState(true)
  const [imageModal, setImageModal] = useState<string | null>(null)
  const [confirmDelete, setConfirmDelete] = useState<string | null>(null)
  const [confirmUnassignAll, setConfirmUnassignAll] = useState(false)
  const [unassigning, setUnassigning] = useState(false)

  async function loadItems() {
    try {
      const data = await fetchItemsByBin(bin.id)
      setItems(data)
    } catch { /* ignore */ }
    setLoading(false)
  }

  useEffect(() => { loadItems() }, [bin.id])

  async function handleDeleteItem(item: Item) {
    try {
      await deleteItem(item.id, item.photo_url)
      setConfirmDelete(null)
      loadItems()
    } catch { /* ignore */ }
  }

  async function handleUnassignAll() {
    setUnassigning(true)
    try {
      await Promise.all(items.map(item => updateItem(item.id, { bin_id: null })))
      loadItems()
    } catch { /* ignore */ }
    setUnassigning(false)
    setConfirmUnassignAll(false)
  }

  return (
    <div>
      <button onClick={onBack} className="flex items-center gap-2 text-gray-400 hover:text-gray-200 mb-4 transition-colors">
        <FaArrowLeft className="w-3.5 h-3.5" />
        <span className="text-sm">Back to bins</span>
      </button>

      <div className="relative mb-6 cursor-pointer" onClick={() => setImageModal(bin.photo_url)}>
        <img src={bin.photo_url} alt={bin.label} className="w-full h-56 object-cover rounded-2xl" />
        <div className="absolute inset-0 bg-linear-to-t from-black/60 to-transparent rounded-2xl" />
        <h2 className="absolute bottom-4 left-4 text-2xl font-bold text-white">{bin.label}</h2>
      </div>

      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-3">
          <h3 className="text-gray-300 font-medium">Items</h3>
          {items.length > 0 && (
            <span className="text-xs px-2 py-0.5 rounded-full bg-purple-500/20 text-purple-300">
              {items.length}
            </span>
          )}
        </div>
        {items.length > 0 && (
          <button
            onClick={() => setConfirmUnassignAll(true)}
            disabled={unassigning}
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs rounded-lg bg-gray-800 hover:bg-gray-700 text-gray-400 hover:text-gray-200 transition-colors"
          >
            <FaLinkSlash className="w-3 h-3" />
            Unassign all
          </button>
        )}
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="w-6 h-6 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : items.length === 0 ? (
        <p className="text-center text-gray-600 py-12">No items in this bin</p>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
          {items.map((item) => (
            <div key={item.id} className="group relative bg-gray-900/60 border border-gray-800 rounded-xl overflow-hidden">
              <img
                src={item.photo_url}
                alt={item.label}
                className="w-full h-28 object-cover cursor-pointer"
                onClick={() => setImageModal(item.photo_url)}
              />
              <div className="p-2">
                <p className="text-sm text-gray-200 truncate">{item.label}</p>
              </div>
              <button
                onClick={() => setConfirmDelete(item.id)}
                className="absolute top-1.5 right-1.5 p-1.5 rounded-full bg-black/60 text-red-400 opacity-0 group-hover:opacity-100 transition-opacity"
              >
                <FaTrash className="w-3 h-3" />
              </button>

              {confirmDelete === item.id && (
                <div className="absolute inset-0 flex flex-col items-center justify-center bg-black/80 backdrop-blur-sm rounded-xl">
                  <p className="text-xs text-gray-300 mb-2">Delete?</p>
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleDeleteItem(item)}
                      className="px-3 py-1 text-xs rounded-lg bg-red-500/20 text-red-300 hover:bg-red-500/30"
                    >
                      Yes
                    </button>
                    <button
                      onClick={() => setConfirmDelete(null)}
                      className="px-3 py-1 text-xs rounded-lg bg-gray-700 text-gray-300 hover:bg-gray-600"
                    >
                      No
                    </button>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {confirmUnassignAll && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
          <div className="bg-gray-900 border border-gray-700 rounded-2xl p-6 max-w-sm w-full mx-4 text-center">
            <p className="text-gray-200 font-medium mb-1">Unassign All Items?</p>
            <p className="text-sm text-gray-500 mb-5">
              {items.length} item{items.length === 1 ? '' : 's'} will be removed from this bin.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setConfirmUnassignAll(false)}
                className="flex-1 py-2 text-sm rounded-xl bg-gray-800 hover:bg-gray-700 text-gray-300 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleUnassignAll}
                disabled={unassigning}
                className="flex-1 py-2 text-sm rounded-xl bg-red-500/20 hover:bg-red-500/30 text-red-300 transition-colors disabled:opacity-60"
              >
                {unassigning ? 'Unassigning...' : 'Unassign All'}
              </button>
            </div>
          </div>
        </div>
      )}
      {imageModal && <ImageModal src={imageModal} onClose={() => setImageModal(null)} />}
    </div>
  )
}

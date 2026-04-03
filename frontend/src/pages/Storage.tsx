import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router'
import { StorageAuthProvider, useStorageAuth } from '../context/StorageAuthContext'
import { FaGoogle, FaGithub, FaPlus, FaBox, FaBoxesStacked, FaTrash, FaArrowRightFromBracket, FaMagnifyingGlass } from 'react-icons/fa6'
import { fetchBins, fetchItems, deleteBin, deleteItem, countItemsInBin, type Bin, type Item } from '../lib/storage-api'
import CreateBinModal from '../components/storage/CreateBinModal'
import CreateItemModal from '../components/storage/CreateItemModal'
import BinDetail from '../components/storage/BinDetail'
import ImageModal from '../components/storage/ImageModal'
import { useToast } from '../context/ToastContext'

export default function Storage() {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/" replace />
  }

  return (
    <StorageAuthProvider>
      <StorageInner />
    </StorageAuthProvider>
  )
}

type Tab = 'bins' | 'items'

function StorageInner() {
  const { isAuthenticated, isLoading, signInWithGoogle, signInWithGithub, signOut, user } = useStorageAuth()
  const toast = useToast()

  const [tab, setTab] = useState<Tab>('bins')
  const [bins, setBins] = useState<Bin[]>([])
  const [items, setItems] = useState<Item[]>([])
  const [binCounts, setBinCounts] = useState<Record<string, number>>({})
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')

  const [showCreateBin, setShowCreateBin] = useState(false)
  const [showCreateItem, setShowCreateItem] = useState(false)
  const [selectedBin, setSelectedBin] = useState<Bin | null>(null)
  const [imageModal, setImageModal] = useState<string | null>(null)
  const [confirmDelete, setConfirmDelete] = useState<string | null>(null)

  async function loadBins() {
    try {
      const data = await fetchBins()
      setBins(data)
      const counts: Record<string, number> = {}
      await Promise.all(data.map(async (bin) => {
        counts[bin.id] = await countItemsInBin(bin.id)
      }))
      setBinCounts(counts)
    } catch { /* ignore */ }
  }

  async function loadItems() {
    try {
      setItems(await fetchItems())
    } catch { /* ignore */ }
  }

  async function loadAll() {
    setLoading(true)
    await Promise.all([loadBins(), loadItems()])
    setLoading(false)
  }

  useEffect(() => {
    if (isAuthenticated) loadAll()
  }, [isAuthenticated])

  async function handleDeleteBin(bin: Bin) {
    const count = await countItemsInBin(bin.id)
    if (count > 0) {
      toast(`Cannot delete — bin has ${count} item${count === 1 ? '' : 's'}`)
      setConfirmDelete(null)
      return
    }
    try {
      await deleteBin(bin.id, bin.photo_url)
      setConfirmDelete(null)
      loadBins()
    } catch { /* ignore */ }
  }

  async function handleDeleteItem(item: Item) {
    try {
      await deleteItem(item.id, item.photo_url)
      setConfirmDelete(null)
      loadItems()
    } catch { /* ignore */ }
  }

  // Loading state for Supabase auth
  if (isLoading) {
    return (
      <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  // Supabase sign-in
  if (!isAuthenticated) {
    return (
      <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-6">
        <div className="bg-gray-900/60 backdrop-blur-sm border border-gray-800 rounded-2xl p-8 w-full max-w-sm text-center">
          <FaBoxesStacked className="w-10 h-10 mx-auto mb-3 text-purple-400" />
          <h2 className="text-xl font-semibold text-gray-100 mb-1">Organizer</h2>
          <p className="text-sm text-gray-500 mb-6">Sign in to manage your storage</p>

          <div className="space-y-3">
            <button
              onClick={signInWithGoogle}
              className="w-full flex items-center justify-center gap-3 py-2.5 rounded-xl bg-white text-gray-900 font-medium hover:bg-gray-100 transition-colors"
            >
              <FaGoogle className="w-4 h-4" />
              Sign in with Google
            </button>
            <button
              onClick={signInWithGithub}
              className="w-full flex items-center justify-center gap-3 py-2.5 rounded-xl bg-[#010409] text-white font-medium hover:bg-[#161b22] transition-colors border border-gray-700"
            >
              <FaGithub className="w-4 h-4" />
              Sign in with GitHub
            </button>
          </div>
        </div>
      </div>
    )
  }

  // Bin detail view
  if (selectedBin) {
    return (
      <div className="p-6 max-w-6xl mx-auto">
        <BinDetail
          bin={selectedBin}
          onBack={() => { setSelectedBin(null); loadAll() }}
          onDeleted={() => { setSelectedBin(null); loadBins() }}
        />
        {imageModal && <ImageModal src={imageModal} onClose={() => setImageModal(null)} />}
      </div>
    )
  }

  const filteredBins = search
    ? bins.filter(b => b.label.toLowerCase().includes(search.toLowerCase()))
    : bins
  const filteredItems = search
    ? items.filter(i => i.label.toLowerCase().includes(search.toLowerCase()))
    : items

  return (
    <div className="p-6 max-w-6xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold text-gray-100">Organizer</h1>
          <p className="text-xs text-gray-500">{user?.email}</p>
        </div>
        <button
          onClick={signOut}
          className="flex items-center gap-2 px-3 py-1.5 text-xs rounded-lg bg-gray-800 hover:bg-gray-700 text-gray-400 hover:text-gray-200 transition-colors"
        >
          <FaArrowRightFromBracket className="w-3 h-3" />
          Sign out
        </button>
      </div>

      {/* Search */}
      <div className="relative mb-5">
        <FaMagnifyingGlass className="absolute left-3.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-gray-500" />
        <input
          type="text"
          placeholder="Search..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-10 pr-4 py-2.5 bg-gray-900/60 border border-gray-800 rounded-xl text-gray-200 placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-purple-400/30"
        />
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-5 bg-gray-900/60 border border-gray-800 rounded-xl p-1">
        <button
          onClick={() => setTab('bins')}
          className={`flex-1 flex items-center justify-center gap-2 py-2 rounded-lg text-sm font-medium transition-colors ${
            tab === 'bins' ? 'bg-purple-500/20 text-purple-300' : 'text-gray-500 hover:text-gray-300'
          }`}
        >
          <FaBoxesStacked className="w-3.5 h-3.5" />
          Bins ({bins.length})
        </button>
        <button
          onClick={() => setTab('items')}
          className={`flex-1 flex items-center justify-center gap-2 py-2 rounded-lg text-sm font-medium transition-colors ${
            tab === 'items' ? 'bg-purple-500/20 text-purple-300' : 'text-gray-500 hover:text-gray-300'
          }`}
        >
          <FaBox className="w-3.5 h-3.5" />
          Items ({items.length})
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-16">
          <div className="w-6 h-6 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : tab === 'bins' ? (
        /* Bins Grid */
        <>
          <div className="flex justify-end mb-4">
            <button
              onClick={() => setShowCreateBin(true)}
              className="flex items-center gap-1.5 px-4 py-2 text-sm rounded-xl bg-purple-500/20 hover:bg-purple-500/30 text-purple-300 transition-colors"
            >
              <FaPlus className="w-3 h-3" />
              New Bin
            </button>
          </div>

          {filteredBins.length === 0 ? (
            <div className="text-center py-16">
              <FaBoxesStacked className="w-10 h-10 mx-auto mb-3 text-gray-700" />
              <p className="text-gray-500">
                {search ? 'No bins match your search' : 'No bins yet — create one to get started'}
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
              {filteredBins.map((bin) => (
                <div
                  key={bin.id}
                  className="group relative bg-gray-900/60 border border-gray-800 rounded-xl overflow-hidden cursor-pointer hover:border-gray-700 transition-colors"
                  onClick={() => setSelectedBin(bin)}
                >
                  <img src={bin.photo_url} alt={bin.label} className="w-full h-36 object-cover" />
                  <div className="p-3">
                    <p className="text-sm text-gray-200 truncate">{bin.label}</p>
                    <p className="text-xs text-gray-500 mt-0.5">
                      {binCounts[bin.id] ?? 0} item{(binCounts[bin.id] ?? 0) === 1 ? '' : 's'}
                    </p>
                  </div>
                  {(binCounts[bin.id] ?? 0) === 0 && (
                    <button
                      onClick={(e) => { e.stopPropagation(); setConfirmDelete(bin.id) }}
                      className="absolute top-2 right-2 p-1.5 rounded-full bg-black/60 text-red-400 opacity-0 group-hover:opacity-100 transition-opacity"
                    >
                      <FaTrash className="w-3 h-3" />
                    </button>
                  )}
                  {confirmDelete === bin.id && (
                    <div
                      className="absolute inset-0 flex flex-col items-center justify-center bg-black/80 backdrop-blur-sm rounded-xl"
                      onClick={(e) => e.stopPropagation()}
                    >
                      <p className="text-xs text-gray-300 mb-2">Delete this bin?</p>
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleDeleteBin(bin)}
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
        </>
      ) : (
        /* Items Grid */
        <>
          <div className="flex justify-end mb-4">
            <button
              onClick={() => setShowCreateItem(true)}
              className="flex items-center gap-1.5 px-4 py-2 text-sm rounded-xl bg-purple-500/20 hover:bg-purple-500/30 text-purple-300 transition-colors"
            >
              <FaPlus className="w-3 h-3" />
              New Item
            </button>
          </div>

          {filteredItems.length === 0 ? (
            <div className="text-center py-16">
              <FaBox className="w-10 h-10 mx-auto mb-3 text-gray-700" />
              <p className="text-gray-500">
                {search ? 'No items match your search' : 'No items yet — create one to get started'}
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
              {filteredItems.map((item) => (
                <div
                  key={item.id}
                  className="group relative bg-gray-900/60 border border-gray-800 rounded-xl overflow-hidden"
                >
                  <img
                    src={item.photo_url}
                    alt={item.label}
                    className="w-full h-36 object-cover cursor-pointer"
                    onClick={() => setImageModal(item.photo_url)}
                  />
                  <div className="p-3">
                    <p className="text-sm text-gray-200 truncate">{item.label}</p>
                    {item.bin_id && (
                      <p className="text-xs text-purple-400 mt-0.5 truncate">
                        {bins.find(b => b.id === item.bin_id)?.label ?? 'In a bin'}
                      </p>
                    )}
                  </div>
                  <button
                    onClick={() => setConfirmDelete(item.id)}
                    className="absolute top-2 right-2 p-1.5 rounded-full bg-black/60 text-red-400 opacity-0 group-hover:opacity-100 transition-opacity"
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
        </>
      )}

      {/* Modals */}
      {showCreateBin && (
        <CreateBinModal
          onClose={() => setShowCreateBin(false)}
          onCreated={loadAll}
        />
      )}
      {showCreateItem && (
        <CreateItemModal
          onClose={() => setShowCreateItem(false)}
          onCreated={loadAll}
        />
      )}
      {imageModal && <ImageModal src={imageModal} onClose={() => setImageModal(null)} />}
    </div>
  )
}

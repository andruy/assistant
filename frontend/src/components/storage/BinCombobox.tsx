import { useState, useRef, useEffect } from 'react'
import { FaChevronDown, FaXmark } from 'react-icons/fa6'
import type { Bin } from '../../lib/storage-api'

interface BinComboboxProps {
  bins: Bin[]
  selectedId: string
  onChange: (id: string) => void
}

export default function BinCombobox({ bins, selectedId, onChange }: BinComboboxProps) {
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState('')
  const inputRef = useRef<HTMLInputElement>(null)
  const containerRef = useRef<HTMLDivElement>(null)

  const selected = bins.find(b => b.id === selectedId)

  const filtered = query
    ? bins.filter(b => b.label.toLowerCase().includes(query.toLowerCase()))
    : bins

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false)
        setQuery('')
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  function handleSelect(id: string) {
    onChange(id)
    setOpen(false)
    setQuery('')
  }

  return (
    <div ref={containerRef} className="relative">
      {/* Display / trigger */}
      <button
        type="button"
        onClick={() => { setOpen(!open); setTimeout(() => inputRef.current?.focus(), 0) }}
        className="w-full flex items-center gap-3 px-4 py-2.5 bg-gray-800/60 border border-gray-700 rounded-xl text-left focus:outline-none focus:ring-2 focus:ring-purple-400/50"
      >
        {selected ? (
          <>
            <img src={selected.photo_url} alt="" className="w-7 h-7 rounded object-cover shrink-0" />
            <span className="text-gray-100 truncate flex-1">{selected.label}</span>
            <button
              type="button"
              onClick={(e) => { e.stopPropagation(); onChange('') }}
              className="p-0.5 rounded hover:bg-gray-700 text-gray-500"
            >
              <FaXmark className="w-3 h-3" />
            </button>
          </>
        ) : (
          <>
            <span className="text-gray-500 flex-1">No bin</span>
            <FaChevronDown className="w-3 h-3 text-gray-600" />
          </>
        )}
      </button>

      {/* Dropdown */}
      {open && (
        <div className="absolute z-10 mt-1.5 w-full bg-gray-900 border border-gray-700 rounded-xl shadow-xl overflow-hidden">
          <div className="p-2">
            <input
              ref={inputRef}
              type="text"
              placeholder="Search bins..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="w-full px-3 py-2 bg-gray-800/80 border border-gray-700 rounded-lg text-sm text-gray-100 placeholder-gray-500 focus:outline-none focus:ring-1 focus:ring-purple-400/50"
            />
          </div>

          <div className="max-h-48 overflow-y-auto">
            <button
              type="button"
              onClick={() => handleSelect('')}
              className={`w-full flex items-center gap-3 px-4 py-2 text-sm hover:bg-gray-800 transition-colors ${
                !selectedId ? 'text-purple-300' : 'text-gray-400'
              }`}
            >
              <div className="w-7 h-7 rounded bg-gray-800 flex items-center justify-center shrink-0">
                <FaXmark className="w-3 h-3 text-gray-600" />
              </div>
              None
            </button>

            {filtered.map(bin => (
              <button
                key={bin.id}
                type="button"
                onClick={() => handleSelect(bin.id)}
                className={`w-full flex items-center gap-3 px-4 py-2 text-sm hover:bg-gray-800 transition-colors ${
                  bin.id === selectedId ? 'text-purple-300' : 'text-gray-200'
                }`}
              >
                <img src={bin.photo_url} alt="" className="w-7 h-7 rounded object-cover shrink-0" />
                <span className="truncate">{bin.label}</span>
              </button>
            ))}

            {filtered.length === 0 && query && (
              <p className="px-4 py-3 text-sm text-gray-600 text-center">No bins match</p>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

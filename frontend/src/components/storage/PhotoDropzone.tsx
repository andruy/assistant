import { useState, useRef, type DragEvent } from 'react'
import { FaCloudArrowUp, FaXmark } from 'react-icons/fa6'

interface PhotoDropzoneProps {
  file: File | null
  onFileSelect: (file: File | null) => void
  compact?: boolean
}

export default function PhotoDropzone({ file, onFileSelect, compact }: PhotoDropzoneProps) {
  const [isDragging, setIsDragging] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  const preview = file ? URL.createObjectURL(file) : null

  function handleDrag(e: DragEvent) {
    e.preventDefault()
    e.stopPropagation()
  }

  function handleDragIn(e: DragEvent) {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(true)
  }

  function handleDragOut(e: DragEvent) {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(false)
  }

  function handleDrop(e: DragEvent) {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(false)
    const files = e.dataTransfer.files
    if (files.length > 0 && files[0].type.startsWith('image/')) {
      onFileSelect(files[0])
    }
  }

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const files = e.target.files
    if (files && files.length > 0) {
      onFileSelect(files[0])
    }
  }

  if (preview) {
    return (
      <div className="relative group">
        <img
          src={preview}
          alt="Preview"
          className={`w-full rounded-xl object-cover ${compact ? 'h-32' : 'h-48'}`}
        />
        <button
          type="button"
          onClick={() => onFileSelect(null)}
          className="absolute top-2 right-2 p-1.5 rounded-full bg-black/60 text-white opacity-0 group-hover:opacity-100 transition-opacity"
        >
          <FaXmark className="w-3.5 h-3.5" />
        </button>
      </div>
    )
  }

  return (
    <>
      <div
        onDragEnter={handleDragIn}
        onDragLeave={handleDragOut}
        onDragOver={handleDrag}
        onDrop={handleDrop}
        onClick={() => inputRef.current?.click()}
        className={`
          flex flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed cursor-pointer transition-colors
          ${compact ? 'h-32' : 'h-40'}
          ${isDragging
            ? 'border-purple-400 bg-purple-400/10'
            : 'border-gray-700 hover:border-gray-500 bg-gray-900/30'
          }
        `}
      >
        <FaCloudArrowUp className={`${compact ? 'w-6 h-6' : 'w-8 h-8'} text-gray-500`} />
        <p className="text-sm text-gray-500">
          Drop image or <span className="text-purple-400">browse</span>
        </p>
      </div>
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        onChange={handleChange}
        className="hidden"
      />
    </>
  )
}

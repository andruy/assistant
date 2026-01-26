export default function About() {
  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4">
      <div className="max-w-2xl w-full">
        <div className="bg-gray-900/60 backdrop-blur-sm border border-gray-800 rounded-2xl p-8 md:p-12 shadow-2xl shadow-purple-500/5">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl md:text-4xl font-bold tracking-wider bg-gradient-to-r from-blue-400 via-purple-400 to-violet-400 bg-clip-text text-transparent mb-4">
              ABOUT NEXUS
            </h1>
            <div className="h-1 w-20 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full" />
          </div>

          {/* Content */}
          <div className="space-y-6 text-gray-300 leading-relaxed">
            <p>
              Welcome to <span className="text-purple-400 font-semibold">NEXUS</span> - your gateway
              to the future of digital experiences. Built with cutting-edge technology and designed
              for the modern era.
            </p>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 my-8">
              <div className="p-4 rounded-xl bg-gray-800/50 border border-gray-700">
                <div className="text-blue-400 font-bold text-2xl mb-1">React 19</div>
                <div className="text-xs text-gray-500 tracking-wide">FRONTEND FRAMEWORK</div>
              </div>
              <div className="p-4 rounded-xl bg-gray-800/50 border border-gray-700">
                <div className="text-purple-400 font-bold text-2xl mb-1">TypeScript</div>
                <div className="text-xs text-gray-500 tracking-wide">TYPE SAFETY</div>
              </div>
              <div className="p-4 rounded-xl bg-gray-800/50 border border-gray-700">
                <div className="text-violet-400 font-bold text-2xl mb-1">Tailwind</div>
                <div className="text-xs text-gray-500 tracking-wide">STYLING ENGINE</div>
              </div>
              <div className="p-4 rounded-xl bg-gray-800/50 border border-gray-700">
                <div className="text-indigo-400 font-bold text-2xl mb-1">Java</div>
                <div className="text-xs text-gray-500 tracking-wide">BACKEND API</div>
              </div>
            </div>

            <p className="text-gray-400 text-sm">
              Designed with a focus on performance, security, and user experience.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

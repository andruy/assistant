import Aurora from "./Aurora";

export default function BackgroundCanvas() {
  return (
    <div className="fixed inset-0 -z-10">
      <Aurora
        colorStops={["#5227FF", "#7cff67", "#5227FF"]}
        amplitude={1}
        blend={1}
      />
    </div>
  )
}

import { useRef, useEffect, useState, forwardRef } from 'react'
import reactLogo from '../assets/react.svg'

const MenuButton = forwardRef((props, ref) => {
    const [angle, setAngle] = useState(0)
    const intervalIdRef = useRef(null)
    const normalPace = 0.36
    const fastPace = 1.35
    const veryFastPace = 7.2

    useEffect(() => {
        setTimeout(() => {
            if (ref.current && ref.current.classList.contains('animate-intro')) {
                ref.current.classList.remove('animate-intro')
            }
            const interval = setInterval(() => {
                setAngle(prevAngle => prevAngle + normalPace) // Steady pace: 360 degrees in 10 seconds
            }, 10)
            intervalIdRef.current = interval
    
            return () => clearInterval(interval)
        }, 1000)
    }, [])

    const setRotationInterval = pace => {
        clearInterval(intervalIdRef.current)
        const interval = setInterval(() => {
            setAngle(prevAngle => prevAngle + pace)
        }, 10)
        intervalIdRef.current = interval
    }

    const handleMouseEnter = () => {
        setRotationInterval(fastPace) // Faster pace: 360 degrees in 5 seconds
    }

    const handleMouseLeave = () => {
        setRotationInterval(normalPace) // Steady pace: 360 degrees in 10 seconds
    }

    const handleClick = () => {
        setRotationInterval(veryFastPace) // Much faster pace: 360 degrees in 1 second

        setTimeout(() => {
            setRotationInterval(normalPace) // Revert to steady pace after 1 second
        }, 300)
    }

    return (
        <img
            ref={ref}
            onClick={handleClick}
            onMouseEnter={handleMouseEnter}
            onMouseLeave={handleMouseLeave}
            src={reactLogo}
            className="react-logo animate-intro"
            alt="React logo"
            style={{ transform: `rotate(${angle}deg)` }}
            data-bs-toggle="collapse"
            data-bs-target="#collapseExample"
            aria-expanded="false"
            aria-controls="collapseExample"
        />
    )
})

export default MenuButton

import { useEffect, useState } from "react";
import "./Ambulance.css";
import useNearbyHospitals from "../hooks/useNearbyHospitals";
import HospitalSelector from "../components/HospitalSelector";
import AmbulanceTracker from "../components/AmbulanceTracker";

const Ambulance = () => {

    const [location, setLocation] = useState({
        lat: null,
        lng: null
    });

    const [showSelector, setShowSelector] = useState(false);

    const [selectedHospital, setSelectedHospital] = useState(null);

    const [ambulanceDetails, setAmbulanceDetails] = useState(null);

    useEffect(() => {

        navigator.geolocation.getCurrentPosition(

            (position) => {

                setLocation({
                    lat: position.coords.latitude,
                    lng: position.coords.longitude
                });

            },

            (err) => {

                console.log(err);

                alert("Please allow location access.");

            }

        );

    }, []);

    const {

        hospitals,

        loading,

        error

    } = useNearbyHospitals(

        location.lat,

        location.lng

    );

    const handleOneTap = () => {

        if (!location.lat || !location.lng) {

            alert("Fetching your location...");

            return;

        }

        if (hospitals.length === 0) {

            alert("No nearby hospitals found.");

            return;

        }

        setShowSelector(true);

    };

    const handleHospitalSelect = (hospital) => {

        setSelectedHospital(hospital);

        setShowSelector(false);

        alert(
            `${hospital.name} selected.\n\nReserving bed & dispatching ambulance...`
        );

        // Call the new AmbulanceServlet
        fetch("http://localhost:8084/Hackathon/api/book-ambulance", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                hospitalId: hospital.id,
                userId: 1,
                userLat: location.lat,
                userLng: location.lng
            })
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert("Bed Reserved & Ambulance Dispatched!\n" + data.message);
                setAmbulanceDetails(data.ambulance);
            } else {
                alert("Failed: " + data.message);
            }
        })
        .catch(err => {
            alert("Error: " + err.message);
        });

    };

    return (

        <div className="ambulance-page">

            <div className="hero">

                <h1>🚑 One Tap Ambulance</h1>

                <p>

                    Press the button below.

                    We'll automatically locate nearby hospitals,

                    reserve a bed,

                    and dispatch an ambulance.

                </p>

                {!ambulanceDetails && (

                    <button
                        className="tap-btn"
                        onClick={handleOneTap}
                    >

                        Request Ambulance

                    </button>

                )}

            </div>

            {loading && (

                <h2 style={{ textAlign: "center" }}>

                    Searching Nearby Hospitals...

                </h2>

            )}

            {error && (

                <h2 style={{ textAlign: "center", color: "red" }}>

                    {error}

                </h2>

            )}

            {

                showSelector && (

                    <HospitalSelector

                        hospitals={hospitals}

                        onClose={() => setShowSelector(false)}

                        onSelect={handleHospitalSelect}

                    />

                )

            }

            {

                selectedHospital && !ambulanceDetails && (

                    <div className="selected-card">

                        <h2>

                            Selected Hospital

                        </h2>

                        <h3>

                            {selectedHospital.name}

                        </h3>

                        <p>

                            📍 {selectedHospital.address}

                        </p>

                        <p>

                            🛏 Beds Available :

                            {selectedHospital.availableBeds}

                        </p>

                        <p>

                            Waiting for ambulance dispatch...

                        </p>

                    </div>

                )

            }

            {

                ambulanceDetails && (

                    <AmbulanceTracker

                        hospital={selectedHospital}

                        userLocation={location}

                        ambulanceDetails={ambulanceDetails}

                    />

                )

            }

        </div>

    );

};

export default Ambulance;
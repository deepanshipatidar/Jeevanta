import { useEffect, useState } from "react";
import "./HospitalSelector.css";

const HospitalSelector = ({ hospitals, onClose, onSelect }) => {

    const [selectedHospital, setSelectedHospital] = useState(
        hospitals[0]?.id || ""
    );

    const [timeLeft, setTimeLeft] = useState(30);

    // Countdown
    useEffect(() => {

        if (timeLeft === 0) {

            const nearestHospital = hospitals[0];

            alert(
                `No hospital selected.\nAutomatically selecting ${nearestHospital.name}`
            );

            onSelect(nearestHospital);

            return;
        }

        const timer = setTimeout(() => {

            setTimeLeft((prev) => prev - 1);

        }, 1000);

        return () => clearTimeout(timer);

    }, [timeLeft, hospitals, onSelect]);

    const handleConfirm = () => {

        const hospital = hospitals.find(
            (h) => h.id === selectedHospital
        );

        onSelect(hospital);

    };

    return (

        <div className="modal-overlay">

            <div className="hospital-modal">

                <h2>Select Hospital</h2>

                <p className="timer">

                    Auto selecting in

                    <span> {timeLeft}s</span>

                </p>

                <div className="hospital-options">

                    {hospitals.map((hospital) => (

                        <label
                            key={hospital.id}
                            className="hospital-option"
                        >

                            <input
                                type="radio"
                                name="hospital"
                                checked={
                                    selectedHospital === hospital.id
                                }
                                onChange={() =>
                                    setSelectedHospital(hospital.id)
                                }
                            />

                            <div>

                                <h4>{hospital.name}</h4>

                                <p>{hospital.address}</p>

                                <small>

                                    Beds Available :

                                    {hospital.availableBeds}

                                </small>

                            </div>

                        </label>

                    ))}

                </div>

                <div className="buttons">

                    <button
                        className="cancel-btn"
                        onClick={onClose}
                    >
                        Cancel
                    </button>

                    <button
                        className="confirm-btn"
                        onClick={handleConfirm}
                    >
                        Confirm
                    </button>

                </div>

            </div>

        </div>

    );

};

export default HospitalSelector;
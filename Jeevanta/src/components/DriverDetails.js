import "./DriverDetails.css";

const DriverDetails = ({ hospital, ambulanceDetails }) => {

    // Use real data from backend if available, fallback to defaults
    const ambulance = ambulanceDetails || {
        driverName: "Rahul Sharma",
        driverPhone: "+91 9876543220",
        vehicleNumber: "MP09 XX 1111",
        ambulanceType: "Advanced Life Support (ALS)",
        estimatedArrival: "8 minutes"
    };

    return (
        <div className="driver-card">

            <h2>🚑 Ambulance Details</h2>

            <div className="driver-info">

                <div>
                    <h3>Driver Name</h3>
                    <p>{ambulance.driverName}</p>
                </div>

                <div>
                    <h3>Phone Number</h3>
                    <p>{ambulance.driverPhone}</p>
                </div>

                <div>
                    <h3>Vehicle Number</h3>
                    <p>{ambulance.vehicleNumber}</p>
                </div>

                <div>
                    <h3>Ambulance Type</h3>
                    <p>{ambulance.ambulanceType}</p>
                </div>

                <div>
                    <h3>Estimated Arrival</h3>
                    <p>{ambulance.estimatedArrival}</p>
                </div>

            </div>

            <button
                className="call-btn"
                onClick={() => window.open(`tel:${ambulance.driverPhone}`)}
            >
                📞 Call Driver
            </button>

        </div>
    );
};

export default DriverDetails;
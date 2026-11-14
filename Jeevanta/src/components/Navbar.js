import { Link } from "react-router-dom";

const Navbar = () => {
    return (
        <div className="navbar">
            <nav>
                <Link to="/bedbooking">Bed Booking</Link>
                <Link to="/ambulance">Ambulance Tracking</Link>
                <Link to="/doctors">Doctors</Link>
                <Link to="/">AI Consultant</Link>
            </nav>
        </div>
    );
};

export default Navbar;
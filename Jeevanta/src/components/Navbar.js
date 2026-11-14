import { Link } from "react-router-dom";

const Navbar = () => {
    return (
        <div className="navbar">
            <nav>
                <Link to="/login">Login</Link>
                <Link to="/signup">Signup</Link>
                <Link to="/ambulance">Ambulance Tracking</Link>
                <Link to="/bedbooking">Bed Booking</Link>
                <Link to="/doctors">Doctors</Link>
                <Link to="/ai-consult">AI Consultant</Link>

            </nav>
        </div>
    );
};

export default Navbar;
import { Link } from "react-router-dom";

const Header = () => {
    return ( 
    <div className="header">
        <div className="header-logo">
            <img src="/logo.jpeg" alt="Jeevanta Logo" />
        </div>
        <div className="header-title">
            <h1>Jeevanta</h1>
            <p>For your Health</p>
        </div>
        <div className="header-auth">
            <Link to="/login" className="btn-login">Login</Link>
            <Link to="/signup" className="btn-signup">Signup</Link>
        </div>
    </div> 
    );
}
 
export default Header;
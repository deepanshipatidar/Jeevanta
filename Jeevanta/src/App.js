import { BrowserRouter, Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar";
import BedBooking from "./pages/BedBooking";
import Ambulance from "./pages/Ambulance";
import Medicines from "./pages/Medicines";
import AIConsultant from "./pages/AIConsultant";
import Header from "./components/Header";
import Doctors from "./pages/Doctors"

import Login from "./pages/Login";
import Signup from "./pages/Signup";

function App() {
    return (
        <BrowserRouter>
            <Header />
            <Navbar />
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />
                <Route path="/bedbooking" element={<BedBooking />} />
                <Route path="/ambulance" element={<Ambulance />} />
                <Route path="/meds" element={<Medicines />} />
                <Route path="/" element={<AIConsultant />} />
                <Route path="/doctors" element={<Doctors />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
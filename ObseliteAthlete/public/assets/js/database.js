<script type="module">
    import {initializeApp} from "https://www.gstatic.com/firebasejs/12.4.0/firebase-app.js";
    import {getAuth, createUserWithEmailAndPassword, signInWithEmailAndPassword, signOut, onAuthStateChanged} from "https://www.gstatic.com/firebasejs/12.4.0/firebase-auth.js";
    import {getDatabase, ref, push, set, onValue, serverTimestamp} from "https://www.gstatic.com/firebasejs/12.4.0/firebase-database.js";

    const firebaseConfig = {
        apiKey: "AIzaSyDjvzocgNxRbVq2Re4P4wPrlFYUXLqQVIs",
    authDomain: "oawebsite-569ef.firebaseapp.com",
    projectId: "oawebsite-569ef",
    storageBucket: "oawebsite-569ef.appspot.com",
    messagingSenderId: "743749509769",
    appId: "1:743749509769:web:6b56c73620951d59229c58",
    databaseURL: "https://oawebsite-569ef-default-rtdb.firebaseio.com/"
};

    const app = initializeApp(firebaseConfig);
    const auth = getAuth(app);
    const db = getDatabase(app);

    async function signup(email, password) {
    return createUserWithEmailAndPassword(auth, email, password);
}

    async function login(email, password) {
    return signInWithEmailAndPassword(auth, email, password);
}

    async function logout() {
    return signOut(auth);
}

    function onAuthChange(cb) {
    return onAuthStateChanged(auth, cb);
}

    function getCurrentUser() {
    return auth.currentUser || null;
}

    async function saveDonation(donation) {
    const user = getCurrentUser();
    if(!user) throw new Error("User must be signed in to save donation");

    const donationsRef = ref(db, "donations");
    const newRef = push(donationsRef);
    const payload = {
        name: donation.name || null,
    email: donation.email || null,
    phone: donation.phone || null,
    amount: donation.amount || null,
    payment_reference: donation.payment_reference || null,
    date: donation.date || null,
    uid: user.uid,
    payfastResponse: donation.payfastResponse || null,
    createdAt: serverTimestamp()
    };
    await set(newRef, payload);
    return newRef.key;
}

    function listenDonations(cb) {
    const donationsRef = ref(db, "donations");
    onValue(donationsRef, snapshot => cb(snapshot.val() || { }));
}

    window.DBApp = {
        signup,
        login,
        logout,
        onAuthChange,
        getCurrentUser,
        saveDonation,
        listenDonations
    };
</script>

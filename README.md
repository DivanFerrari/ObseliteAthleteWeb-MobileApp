# 🏃‍♂️ Obselite Athlete Foundation Website

[![Firebase](https://img.shields.io/badge/Firebase-Hosting-yellow?style=for-the-badge&logo=firebase)](https://firebase.google.com/)
[![JavaScript](https://img.shields.io/badge/JavaScript-ES6+-yellow?style=for-the-badge&logo=javascript)](https://developer.mozilla.org/en-US/docs/Web/JavaScript)
[![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/HTML)
[![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/CSS)
[![PayFast](https://img.shields.io/badge/PayFast-Payment%20Gateway-blue?style=for-the-badge&logo=paypal)](https://www.payfast.co.za/)

## 📋 Table of Contents

- [Overview](#-overview)
- [✨ Features](#-features)
- [🏗️ Project Structure](#️-project-structure)
- [🚀 Getting Started](#-getting-started)
- [🔧 Configuration](#-configuration)
- [📱 Pages](#-pages)
- [👥 User Roles](#-user-roles)
- [💳 Payment Integration](#-payment-integration)
- [🖼️ Gallery Management](#️-gallery-management)
- [🔐 Authentication](#-authentication)
- [📦 Deployment](#-deployment)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)
- [📞 Support](#-support)

## 🌟 Overview

Obselite Athlete is a youth-driven sports and wellness training facility supporting vulnerable youth in Nelson Mandela Bay, South Africa. This website serves as the digital presence for the foundation, providing information about programs, accepting donations, and showcasing events through an interactive gallery.

The platform enables:
- **Community engagement** through event information and contact forms
- **Secure donations** via PayFast payment gateway
- **Photo gallery** for event documentation
- **User authentication** for personalized experiences
- **Admin panel** for content management

## ✨ Features

### 🎯 Core Features
- **Responsive Design** - Mobile-first approach ensuring accessibility on all devices
- **Dark Theme** - Modern, eye-friendly dark theme with red accent colors
- **Real-time Authentication** - Firebase Auth integration for secure user management
- **Dynamic Content** - Real-time updates without page refreshes

### 💰 Donation System
- **Secure Payments** - Integration with PayFast South Africa
- **Multiple Amounts** - Preset amounts (R50, R100, R250, R500) and custom entry
- **Instant Processing** - Real-time payment processing with redirect handling
- **Donation Tracking** - Record and manage donation history

### 🖼️ Gallery Management
- **Admin Uploads** - Authorized administrators can upload images
- **Base64 Storage** - Efficient image storage in Firestore
- **Categories** - Organize images (Nature, Urban, People, Other)
- **Search & Filter** - Find images by category or keywords
- **Modal View** - Full-screen image viewing with descriptions

### 👤 User System
- **Registration/Login** - Secure email/password authentication
- **Profile Management** - View and manage user information
- **Role-based Access** - Regular users vs administrators
- **Session Management** - Persistent login state

## 🏗️ Project Structure

```
obselite-athlete/
├── 📁 assets/
│   ├── 📁 css/          # Stylesheet files
│   ├── 📁 imgs/          # Static images and logos
│   ├── 📁 js/            # JavaScript files
│   └── 📁 vendors/       # Third-party libraries
├── 📄 index.html         # Main landing page
├── 📄 Authentication.html # Login/Signup page
├── 📄 donate.html        # Donation page
├── 📄 Gallery.html       # Photo gallery
├── 📄 Profile.html       # User profile page
├── 📄 success.html       # Donation success page
├── 📄 payfast-notify.html # Payment webhook handler
├── 📄 firebase.json      # Firebase hosting config
└── 📄 README.md          # This documentation
```

## 🚀 Getting Started

### Prerequisites

- **Node.js** (v14 or higher)
- **Firebase CLI** (`npm install -g firebase-tools`)
- **Modern web browser** (Chrome, Firefox, Safari, Edge)
- **Code editor** (VS Code recommended)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/obselite-athlete.git
   cd obselite-athlete
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Set up Firebase**
   ```bash
   firebase login
   firebase init
   ```
   - Select Hosting and Firestore
   - Choose your Firebase project
   - Set `public` as your public directory
   - Configure as single-page app (Yes)

4. **Run locally**
   ```bash
   firebase serve
   ```
   Visit `http://localhost:5000`

## 🔧 Configuration

### Firebase Configuration

Update the Firebase config in all pages:

```javascript
const firebaseConfig = {
    apiKey: "YOUR_API_KEY",
    authDomain: "YOUR_AUTH_DOMAIN",
    projectId: "YOUR_PROJECT_ID",
    storageBucket: "YOUR_STORAGE_BUCKET",
    messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
    appId: "YOUR_APP_ID",
    databaseURL: "YOUR_DATABASE_URL"
};
```

### PayFast Configuration

Update merchant details in `donate.html`:

```javascript
const PAYFAST = {
    url: "https://www.payfast.co.za/eng/process",
    merchant_id: "YOUR_MERCHANT_ID",
    merchant_key: "YOUR_MERCHANT_KEY",
    return_url: window.location.origin + "/success.html",
    cancel_url: window.location.origin + "/donate.html",
    notify_url: window.location.origin + "/payfast-notify"
};
```

### Admin Configuration

Set admin user IDs in `Gallery.html`:

```javascript
const ADMIN_USER_IDS = ['USER_ID_1', 'USER_ID_2'];
```

## 📱 Pages

### 🏠 Home Page (`index.html`)
- Hero section with mission statement
- About us section
- Services/offerings cards
- Team member profiles
- Contact form (via Formspree)
- Footer with contact information

### 🔑 Authentication (`Authentication.html`)
- Dual-panel sign in/sign up interface
- Social media login options
- Smooth panel transitions
- Form validation
- Toast notifications for feedback

### 💰 Donate (`donate.html`)
- Preset donation amounts
- Custom amount input
- Donor information form
- PayFast payment integration
- Impact information sidebar
- Back to home navigation

### 🖼️ Gallery (`Gallery.html`)
- **Public View** - Browse all images
- **Admin Features**:
  - Upload images (JPEG, PNG, GIF, WebP)
  - Delete images
  - Filter and search
  - Category management
- Modal view for image details

### 👤 Profile (`Profile.html`)
- User information display
- Account creation date
- Member ID
- Logout functionality
- Navigation back to home

### ✅ Success (`success.html`)
- Donation confirmation
- Links to return home or donate again

## 👥 User Roles

### 👤 Regular Users
- View gallery images
- Make donations
- View their profile
- Browse website content

### 👑 Administrators
- All regular user features
- Upload images to gallery
- Delete images from gallery
- Manage image categories
- Access admin panel

## 💳 Payment Integration

### PayFast Setup

1. **Create PayFast Merchant Account**
   - Register at [PayFast](https://www.payfast.co.za)
   - Get merchant ID and key

2. **Configure Webhook**
   - Set notify URL to `https://yourdomain.com/payfast-notify`
   - Handle payment notifications

3. **Test Payments**
   - Use PayFast sandbox for testing
   - Test with various amount scenarios

## 🖼️ Gallery Management

### Image Upload Specifications
- **Max Size**: 1MB (free Firebase storage limit)
- **Formats**: JPEG, PNG, GIF, WebP
- **Storage**: Base64 encoded in Firestore

### Image Categories
- `nature` - Outdoor and nature photos
- `urban` - City and facility photos
- `people` - Team and participant photos
- `other` - Miscellaneous images

## 🔐 Authentication

### Security Features
- **Email/Password Authentication** - Secure Firebase Auth
- **Session Management** - Persistent login with automatic token refresh
- **Protected Routes** - Role-based access control
- **Secure Data** - Firestore security rules

### Firebase Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Images: authenticated users can read, only admins can write
    match /images/{imageId} {
      allow read: if true;
      allow write: if request.auth != null && 
        request.auth.uid in ['ADMIN_USER_ID_1', 'ADMIN_USER_ID_2'];
    }
  }
}
```

## 📦 Deployment

### Deploy to Firebase Hosting

1. **Build your project** (if using build tools)
   ```bash
   npm run build
   ```

2. **Deploy to Firebase**
   ```bash
   firebase deploy --only hosting
   ```

3. **Deploy with functions** (if using Cloud Functions)
   ```bash
   firebase deploy
   ```

### Custom Domain Setup

1. Add custom domain in Firebase Console
2. Update DNS records with your domain provider
3. Configure SSL certificate (automatic with Firebase)

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add some amazing feature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open a Pull Request**

### Contribution Guidelines
- Follow existing code style
- Test thoroughly before submitting
- Update documentation as needed
- Keep commits focused and descriptive

## 📄 License

This project is proprietary and confidential. All rights reserved by Obselite Athlete Foundation.

## 📞 Support

### Contact Information

- **📧 Email**: o.athlete07@gmail.com
- **📞 Phone**: 072 642 7624
- **📍 Address**: Port Elizabeth, South Africa

### Technical Support

For technical issues or questions about the website:

- **📧 Email**: o.athlete07@gmail.com
- **🐛 Issues**: [GitHub Issues](https://github.com/yourusername/obselite-athlete/issues)

### Emergency Contacts

For urgent issues related to donations or website functionality, please contact us immediately via phone or email.

---

## 🙏 Acknowledgments

- **Firebase** - Authentication, Firestore, and Hosting
- **PayFast** - Payment processing
- **Font Awesome** - Icons
- **Google Fonts** - Montserrat font family
- **Formspree** - Form handling
- **All Contributors** - Everyone who helps improve this project

---

<div align="center">
  <strong>Made with ❤️ for the youth of Nelson Mandela Bay</strong>
  <br>
  <br>
  <sub>© 2023 Obselite Athlete Foundation. All rights reserved.</sub>
</div>

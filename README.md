# 📱 DIU Lost & Found — Android Application

**DIU Lost & Found** is a dedicated native Android application designed to help students, faculty, and staff of **Dhaka International University (DIU)** report lost items and return found belongings within the campus environment safely and efficiently.

---

## ✨ Key Features

* **🔐 User Authentication:** Secure email-based registration and login system powered by Firebase Authentication.
* **📢 Lost & Found Feeds:** Real-time updates on reported missing and found items on campus.
* **🖼️ Cloud Image Uploads:** Instant image processing and uploading powered by **ImgBB API** (via Retrofit & OkHttp) for lightweight database management without cloud storage costs.
* **⚡ Dynamic Image Loading:** Smooth image caching and rendering using the **Glide** library.
* **🗃️ Real-time Database:** Synchronized text metadata (Titles, Descriptions, Categories, Dates, and Image URLs) stored in **Firebase Firestore**.
* **🧭 Intuitive UI/UX:** Clean, modern, and user-friendly navigation using Bottom Navigation and Fragment Architecture.

---

## 🛠️ Tech Stack & Architecture

* **Language:** Kotlin
* **IDE:** Android Studio
* **Architecture:** Single Activity with Fragment Architecture
* **Backend & Auth:** Firebase Authentication, Firebase Firestore
* **Image Hosting:** ImgBB REST API
* **Networking:** Retrofit2, OkHttp3
* **UI Components & Image Loading:** Jetpack Components, Glide

---

## ⚙️ How It Works (Workflow)

1. **User Auth:** Users log in or register via Firebase Auth.
2. **Item Reporting:** Users fill out lost/found details and select an image from their device.
3. **API Upload:** The image is base64-encoded and sent to the **ImgBB REST API** via `ImgBBUploader.kt`.
4. **Data Sync:** ImgBB returns a direct public image URL, which is saved alongside post details in **Firebase Firestore**.
5. **Feed Render:** `Glide` loads image URLs dynamically into `RecyclerView` cards across the app.

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone [https://github.com/Sawon89/DIU-Lost-And-Found.git](https://github.com/Sawon89/DIU-Lost-And-Found.git)

## Open the project in Android Studio.

  1.Sync Gradle dependencies.

  2.Add your google-services.json file to the app/ directory for Firebase configuration.

  3.Add your ImgBB API Key in ImgBBUploader.kt.

  4.Build and run the app on an emulator or physical device.


## 👨‍💻 Developer
# Md Saifullah Islam Sawon

Department of Computer Science & Engineering (CSE)

Dhaka International University (DIU)

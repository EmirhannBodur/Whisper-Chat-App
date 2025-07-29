# 💬 Whisper - Güvenli ve Modern Mesajlaşma Uygulaması / Secure & Modern Chat App

![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)
![MIT License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

---

## 🇹🇷 Türkçe Açıklama

**Whisper**, modern Jetpack Compose yapısı ve Firebase teknolojileri ile geliştirilmiş, güvenli ve anonim mesajlaşma sağlayan bir Android uygulamasıdır. Telefon numarası paylaşımı olmadan kullanıcılar birbirlerini sadece sistem tarafından verilen ID ile ekleyebilir, medya paylaşımı yapabilir ve gizlilik içinde iletişim kurabilir.

---

## 🇺🇸 English Description

**Whisper** is a modern Android chat application built with Jetpack Compose and Firebase. It enables secure and anonymous messaging without the need to share phone numbers. Users can connect using unique IDs and communicate safely with features like media sharing, profile management, and blocking.

---

## 🚀 Özellikler / Features

| Özellik (TR) | Feature (EN) |
|--------------|--------------|
| 🔐 Firebase Authentication | User registration & login |
| ☁️ Firestore Database | Real-time message syncing |
| 📦 Firebase Storage | Uploading & sending photos, videos, voice |
| 📩 Firebase Cloud Messaging | Push notifications |
| 🎙️ Sesli Mesaj Gönderme | Send voice messages |
| 📷 Fotoğraf / 🎥 Video Gönderme | Media sharing (photo/video) |
| 🖼️ Profil Fotoğrafı Yükleme | Profile picture upload |
| 📝 İsim Değiştirme | Change display name |
| 🔎 Kullanıcı Arama (ID/İsim) | Search users (by ID or name) |
| 🚫 Kullanıcı Engelleme | Block users |
| ➕ Kullanıcı Ekleme (Numara ile) | Add user by unique number |
| ✅ Okundu / Görüldü Durumu | Message seen indicators |
| 🗑️ Mesaj Silme | Delete message (for self or all) |
| 🌓 Tema Desteği (Planlanıyor) | Theme switch (coming soon) |

---

## 📱 Ekran Görüntüleri / Screenshots

<!-- Görsel bağlantılarını eklemeye devam edebilirsin -->
![Ekran1](https://github.com/user-attachments/assets/e9514855-4806-4c83-9905-21e75e6c398b)
![Ekran2](https://github.com/user-attachments/assets/f65258ac-811f-40ec-9186-c3368b3004b4)
![Ekran3](https://github.com/user-attachments/assets/463769bd-b772-4dc8-bc02-8002a2b3d524)
![IMG-20250729-WA0001](https://github.com/user-attachments/assets/379f0d65-bca7-4cc3-9b2f-0e41b1b0991c)
![IMG-20250729-WA0006](https://github.com/user-attachments/assets/a6944931-7b18-4164-9509-48bd12e93634)
![IMG-20250729-WA0002](https://github.com/user-attachments/assets/24e0661d-afd8-4fcd-9315-98ed02a734c8)
![IMG-20250729-WA0004](https://github.com/user-attachments/assets/33ca7995-6cf8-4528-902b-cd68852617bc)
![IMG-20250729-WA0005](https://github.com/user-attachments/assets/8fd50641-833c-445b-883b-b689512d816f)
![WhatsApp Görsel 2025-07-29 saat 20 28 32_3d25d7c8](https://github.com/user-attachments/assets/af7fd70e-3850-42c3-b4bc-bc5050fc672f)

---

## 🧪 Kullanım Senaryosu / Usage Scenario

### 🇹🇷 Türkçe

1. Kullanıcı uygulamaya kayıt olur (e-posta & şifre).
2. Sistem, kullanıcıya benzersiz bir ID (Numara) atar.
3. Diğer kullanıcılar bu numara ile eklenebilir.
4. Uygulama içinden mesajlaşabilir, medya (fotoğraf, video, ses) gönderebilir.
5. Kişiler birbirini engelleyebilir veya profil bilgilerini düzenleyebilir.
6. Bildirim sistemi ile yeni mesajlardan anında haberdar olunur.

### 🇺🇸 English

1. User registers with email and password.
2. The system assigns a unique user number (ID).
3. Users can add each other using this ID.
4. They can chat, send media (images, videos, voice messages).
5. Users can block each other or update their profile.
6. Instant notifications alert for new messages.

---

## ⚙️ Geliştirme Ortamı / Development Stack

- **Kotlin**
- **Android Studio Giraffe**
- **Jetpack Compose**
- **MVVM Architecture**
- **Firebase Suite: Auth, Firestore, Storage, Messaging**
- **Hilt (Dependency Injection)**

---

## 📦 Kurulum / Setup

```bash
git clone https://github.com/kullaniciadi/whisper.git

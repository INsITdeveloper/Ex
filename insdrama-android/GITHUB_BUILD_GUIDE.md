# 🚀 Build APK Otomatis dengan GitHub Actions

## ✅ CARA PALING MUDAH DAPAT APK!

**Tidak perlu Android Studio! Tidak perlu install apapun!**

---

## 📋 **Langkah-Langkah:**

### **1. Buat GitHub Account** (jika belum punya)
```
https://github.com/signup
```

### **2. Upload Project ke GitHub**

**Cara A: Lewat Browser**
```
1. Login ke GitHub
2. Klik "+" → "New repository"
3. Nama: insdrama-android
4. Visibility: Private (atau Public)
5. Klik "Create repository"
6. Upload semua file dari folder insdrama-android
7. Klik "Commit changes"
```

**Cara B: Lewat Git Command**
```bash
cd insdrama-android
git init
git add .
git commit -m "Initial commit - InsDrama"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/insdrama-android.git
git push -u origin main
```

### **3. Enable GitHub Actions**
```
1. Buka repository kamu di GitHub
2. Klik tab "Actions"
3. Klik "I understand my workflows, go ahead and enable them"
```

### **4. Build Otomatis!**
```
Setiap kali kamu push code ke GitHub:
→ GitHub Actions otomatis build APK
→ APK jadi dalam 5-10 menit
→ Download dari Actions tab!
```

### **5. Download APK**
```
1. Klik tab "Actions" di GitHub
2. Pilih workflow run terbaru
3. Scroll ke bawah ke "Artifacts"
4. Klik "INS-Drama-Debug"
5. APK ter-download!
6. Rename jadi INS.apk
7. Install di HP!
```

---

## 🎯 **Auto-Build Triggers:**

APK otomatis di-build saat:
- ✅ Push ke branch main/master
- ✅ Pull request
- ✅ Manual trigger (workflow_dispatch)

**Manual Trigger:**
```
1. Actions tab → "Build InsDrama APK"
2. Klik "Run workflow"
3. Pilih branch
4. Klik "Run workflow"
5. Tunggu 5-10 menit
6. Download APK!
```

---

## 📦 **APK Location:**

Setelah build selesai:
```
GitHub → Actions → Workflow Run → Artifacts → INS-Drama-Debug
```

**File:** `app-debug.apk` (rename jadi `INS.apk`)

---

## ⚡ **Keuntungan GitHub Actions:**

| Benefit | Description |
|---------|-------------|
| **FREE** | 2000 minutes/month gratis |
| **No Install** | Tidak butuh Android Studio |
| **Auto Build** | Setiap push = auto APK |
| **Cloud** | Build di server GitHub (cepat!) |
| **Reliable** | Tidak gagal karena HP kentang |
| **Version Control** | Semua versi APK tersimpan |

---

## 🔧 **Troubleshooting:**

### **Build Failed?**

**Check log:**
```
1. Actions tab → klik workflow run
2. Lihat error di log
3. Screenshot error
4. Kasih tau saya, saya bantu fix!
```

**Common errors:**
- Gradle sync failed → Clear cache, re-run
- SDK not found → Sudah di-handle di workflow
- Out of memory → GitHub handle otomatis

### **Artifact Tidak Muncul?**

```
1. Pastikan workflow selesai (green check)
2. Refresh halaman
3. Check "Artifacts" section di bawah
```

---

## 📱 **Install APK di HP:**

```
1. Download APK dari GitHub Actions
2. Rename: app-debug.apk → INS.apk
3. Transfer ke HP (USB/WhatsApp/Drive)
4. Di HP: Settings → Security → Enable "Unknown Sources"
5. Install APK
6. Done! 🎉
```

---

## 🎨 **Custom Build:**

**Mau build Release (signed) APK?**

Edit `.github/workflows/build.yml`, tambahkan signing:

```yaml
- name: Sign APK
  uses: r0adkll/sign-android-release@v1
  with:
    releaseDirectory: app/build/outputs/apk/release
    signingKeyBase64: ${{ secrets.SIGNING_KEY }}
    alias: ${{ secrets.ALIAS }}
    keyStorePassword: ${{ secrets.KEY_STORE_PASSWORD }}
```

---

## 🚀 **Quick Start:**

```bash
# 1. Init git
cd insdrama-android
git init
git add .
git commit -m "InsDrama v1.0"

# 2. Buat repo di GitHub (lewat browser)

# 3. Push
git remote add origin https://github.com/YOUR_USERNAME/insdrama-android.git
git push -u origin main

# 4. Buka Actions di GitHub → Build otomatis!

# 5. Download APK setelah 5-10 menit
```

---

## 💡 **Tips:**

- **Build time:** 5-10 menit
- **APK size:** ~50-80MB (debug)
- **Retention:** 30 hari (download sebelum expired)
- **Free tier:** 2000 minutes/month (cukup untuk 200+ builds!)

---

**🎉 Ini cara PALING MUDAH dapat APK tanpa Android Studio!**

**Tinggal upload ke GitHub, APK jadi otomatis!** 🚀

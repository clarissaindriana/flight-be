1. ![alt text](<No-1-P09.png>)
![alt text](<No-1.1-P09.png>)

2. ![alt text](<No-2-P09.png>)
Pipeline berjalan setiap kali ada commit ke branch main. Code akan dibangun menggunakan Maven, containerized menggunakan Docker, lalu image dipush ke registry. Setelah itu pipeline melakukan deployment ke Kubernetes cluster melalui SSH ke EC2 instance, dan aplikasi otomatis terupdate dengan versi terbaru.

3. ![alt text](<No-3-P09.png>)
Pada pipeline versi improvement ini, proses penerapan CI/CD dibuat lebih lengkap, aman, dan terstruktur. Setelah developer melakukan push code atau membuka merge request, pipeline akan memulai Static Code Analysis untuk memastikan kualitas kode memenuhi standar. Selanjutnya, proses Build & Test dijalankan termasuk pengecekan code coverage. Setelah itu dilakukan Security Scan pada dependency maupun container untuk mendeteksi vulnerability sejak awal. Jika semua tahap tersebut lolos, pipeline akan membangun dan push Docker image menggunakan penamaan versi yang lebih terkontrol seperti semantic versioning. Kemudian aplikasi akan deploy ke Staging environment terlebih dahulu, dilanjutkan dengan automated smoke test untuk memastikan fitur inti berjalan dengan benar. Sebelum masuk ke produksi, terdapat Manual Approval Gate sebagai pengaman. Setelah disetujui, pipeline melakukan rolling update ke Production environment sehingga tidak ada downtime saat proses deployment. Di akhir pipeline, notifikasi otomatis dikirim ke Slack/Discord agar developer langsung mengetahui status keberhasilan atau kegagalan deployment.

4. EC2 instance perlu dikaitkan dengan Elastic IP agar memiliki static public IP address yang tidak berubah saat instance stop/start. Tanpa Elastic IP, setiap kali instance direstart, akan diberikan dynamic public IP baru sehingga domain, server config, atau external access yang sudah diarahkan ke IP sebelumnya akan gagal. Elastic IP memastikan akses tetap konsisten terutama untuk deployment aplikasi yang harus selalu reachable.
5. Pada praktikum ini, Docker digunakan untuk melakukan containerization, yaitu membungkus aplikasi dan dependency agar bisa dijalankan secara konsisten di berbagai environment. Sementara Kubernetes digunakan sebagai orchestrator, yang mengatur scaling, self-healing, rollout & rollback, serta service networking. Intinya: Docker menjalankan container, Kubernetes mengelola container dalam cluster environment.
6. Proses paling penting dalam keseluruhan pipeline adalah Continuous Deployment step, yaitu otomatisasi deploy ke server setiap ada perubahan pada source code. Ini penting karena memastikan aplikasi selalu up-to-date, mengurangi human error, mempercepat delivery, dan mendukung konsep DevOps “release early, release often”.
7. Kelima file konfigurasi Kubernetes memiliki fungsi berbeda:
* deployment.yaml mengatur definisi Pod dan container image yang dijalankan, termasuk jumlah replica dan update strategy.
* service.yaml mengatur cara Pod diakses dalam cluster melalui service abstraction.
* ingress.yaml (jika ada dalam folder k8s) mengatur routing HTTP request ke service di dalam cluster.
* secret.yaml (dibuat melalui .gitlab-ci.yml) menyimpan informasi sensitif seperti credentials database dalam bentuk encrypted Kubernetes secret.
* config.yaml (dibuat melalui .gitlab-ci.yml) menyimpan konfigurasi environment variables non-sensitive menggunakan ConfigMap.
Dengan pemisahan ini, Kubernetes mendukung separation of concern antara aplikasi, konfigurasi, dan keamanan.
8. Sistem start on restart diterapkan melalui:
* Di Docker, pada bagian docker run atau docker-compose terdapat konfigurasi seperti restart: always yang memastikan container otomatis berjalan kembali saat server reboot.
* Di Kubernetes, mekanisme ReplicaSet pada Deployment akan selalu memastikan jumlah Pod sesuai yang didefinisikan. Jika node restart atau Pod mati, Kubernetes akan otomatis re-create Pod.Jadi, meskipun tidak sadar, konfigurasi tersebut sudah membuat aplikasi self-healing and auto-restart.
9. Keuntungan menggunakan Kubernetes dibanding hanya menjalankan Docker image di server adalah kemampuan scaling, load balancing, rolling update, self-healing, serta isolated networking antar microservices. Kubernetes membuat deployment lebih reliable, maintainable, dan automated, sehingga lebih sesuai untuk production atau environment dengan traffic tidak statis.
10. Ketiga tipe service Kubernetes:
* ClusterIP — default type, hanya bisa diakses internal cluster (tidak memiliki external access).
* NodePort — membuka akses dari luar melalui port di setiap node, cocok untuk testing tetapi kurang aman dan tidak fleksibel.
* LoadBalancer — menyediakan public IP melalui cloud provider untuk akses eksternal dengan balanced traffic.Dalam praktikum ini ClusterIP adalah pilihan tepat karena aplikasi diakses melalui Ingress, sehingga service cukup diekspos dalam internal cluster dan tetap secure.
11. Pelajaran terpenting dari proses deployment otomatis ini adalah bagaimana CI/CD pipeline mampu meningkatkan efisiensi dan mengurangi manual steps dalam development lifecycle. Konsep CI/CD bisa diterapkan untuk proyek lain seperti mobile apps, microservices, atau IoT systems dengan memastikan update berjalan otomatis saat code di-push, termasuk proses testing, build, dan deployment sehingga developer bisa fokus pada fitur tanpa takut deployment error.
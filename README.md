# 🚦 PDD Desktop App

**Приложение для подготовки к экзаменам по ПДД**, реализованное на Kotlin с использованием Compose for Desktop и SQLite.  
Поддерживает регистрацию, прохождение билетов, экзаменов, ведение индивидуальной и общей статистики.

---

## 🧱 Технологии

- 🖥️ Kotlin (JVM)
- 💻 Compose for Desktop (Jetpack Compose)
- 🗃 SQLite + Exposed ORM
- 🔐 Preferences API
- ✅ Архитектура MVVM
- 🌗 Поддержка светлой и тёмной темы

---

## 📂 Структура проекта

```text
.
├── data/
│   ├── db/                # SQLite-таблицы (Users, Statistics) + DatabaseFactory
│   ├── model/             # Data classes (User, StatisticsData)
│   └── repository/        # UserRepository, StatisticsRepository
│
├── viewmodel/             # ViewModel'и для каждого экрана
│   ├── SettingsViewModel.kt
│   └── ...
│
├── ui/
│   ├── screens/           # UI: LoginScreen, TicketsScreen, SettingsScreen и др.
│   └── components/        # Общие компоненты (BottomNav, ThemeToggle)
│
├── utils/                 # Валидация, расширения
├── assets/                # Изображения и иконки
├── AppPreferences.kt      # Хранение авторизации
├── Screen.kt              # Навигация
└── Main.kt                # Точка входа

## 📈 Граф веток (визуально)

🧭 Посмотреть граф веток и историю коммитов можно тут:  
 [GitHub Network Graph](https://github.com/gitmonstera/kp/network)
# Validation report

Дата проверки: 2026-06-09.

## Выполнено успешно

- все Java-файлы `src/main/java` скомпилированы Java 21 против локального набора сигнатурных заглушек Fabric/Minecraft/Gson/SLF4J/Mixin;
- все Java-файлы `src/test/java` скомпилированы против JUnit-сигнатур;
- smoke-тест ядра завершился строкой `CORE_SMOKE_OK`;
- smoke-тест проверил ALL-фильтр, безопасную ошибку некорректного regex и 10-секундный интервал объединения;
- `fabric.mod.json`, mixin JSON и обе локализации разобраны стандартным JSON-парсером;
- все ключи `Text.translatable(...)`, используемые исходниками, существуют в `en_us.json` и `ru_ru.json`;
- поиск по проекту не обнаружил `TODO`, `FIXME`, псевдокода или `UnsupportedOperationException`.

## Настоящая Gradle/Loom-сборка

Команда была запущена:

```text
./gradlew build --stacktrace
```

Но bootstrap Gradle остановился до конфигурации проекта:

```text
curl: (6) Could not resolve host: services.gradle.org
```

Это ограничение сети контейнера, а не результат компиляции против настоящих Minecraft/Fabric зависимостей. Поэтому этот архив нельзя честно помечать как runtime-проверенный JAR. Обязательная финальная проверка на обычной машине:

```bash
./gradlew clean build
./gradlew runClient
```

После запуска следует проверить `run/logs/latest.log`, создание/удаление вкладок, click/hover-события, очистку сервера, action bar и изменение разрешения.

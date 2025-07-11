# SyntheticDataGenerator

Данное приложение позволяет в СУБД создавать схемы с таблицами и записывать в них сгенерируемые синтетические данные. 
Есть возможность указать разнообразные правила генерации синтетичесих данных для каждого атрибута таблиц. Такие правила 
как:
- генерация целочисленных данных с начальным значеним, количеством повторений и шагом дискретизации;
- генерация случайных чисел в заданном диапазоне интервале значений;
- генерация значений из указанного списка, выбранные случайным образом;
- генерация значений дата времянном формате timestamp c начальным значением и шагом дискретизации;
- генерация значений дата времянном формате timestamp случайным образом в заданом интервале времени;
- генерация строковых значений случайным образом фиксированной или не фиксированной длины.

Поддерживаются генерация следующих типы данных: `integer`, `boolean`, `timestamp`, `string`, `varchar`.
А также есть возможность записи последовательности значений в атрибут таблицы с соответсвующей размерностью таблицы.
Приложение поддерживает следующие СУБД: `Postgres`, `Hive` и `Greenplum`. 

Кроме того, приложение поддерживает запуск генерации полу-синтетичесих данных и запись их в поддерживаемые СУБД, а также в формате `JSON` и записи в `Kafka` топики в
потоковом режиме с помощью `Flink`.

Приложение может быть использовано в качестве миграций данных из одной системы в другую.
Подерживаются СУБД (`Postgres`, `Hive` и `Greenplum`) и `Kafka`.

## Release Notes 1.0.0
В рамках релиза был добавлен следующий функционал:
1. Полу-синтетическая генерация данных.
   - Исходные данные загружаются из источника (СУБД или Kafka), трансормация данных и запись в СУБД или в Kafka.
   - Возможность мигрировать данные из одного СУБД в другой или в Kafka с возможностью трансформации данных.
2. Потоковая обработка данных.
   - Возможность запуск приложения в потоковом режиме с помощью Flink.
   - В качестве источника можно указать список топиков Kafka.
   - В качестве целевой системы можно указать один топик Kafka.
   - Возможность мигрировать данные из одного инстанса Kafka в другой с возможностью трансформации данных.
3. Greenplum добавлен в спискок поддерживаемых СУБД.

Появилось возможность запуска следующих сценариев работы приложения:
1. `--mode batch --genType semi-synthetic --srcDataType postgres (1) --targetDataType postgres (1)`
2. `--mode batch --genType semi-synthetic --srcDataType postgres (1) --targetDataType greenplum (1)`
3. `--mode batch --genType semi-synthetic --srcDataType postgres (1) --targetDataType kafka`
4. `--mode stream --genType semi-synthetic --srcDataType kafka (2) --targetDataType kafka (2)`

Примечание:
* (1) В примерах указан СУБД `Postgres`, но может указываться любая другая из списка поддерживаемых СУБД
* (2) Можно как один и тот же инстанс `Kafka`, так и разные инстансы `Kafka`


## Поддерживаемый функционал приложения
1. Генерация синтетических данных согласно указанным правилам (Пример правил `./rulesPostgresExample.json`)
2. Возможность создания множество целых схем со своими таблицами с синетическими данными в `Postgres` и `Hive`
3. Возможность записи синететических данных в формате `JSON` в `Kafka Topic` в потоковом режиме с помощью `Flink` 
4. Поддержка генерация следующих типов данных: `int`, `string` (только для `Hive`), `varchar`, `boolean`, `timestamp`
5. Поддерживаемык правила генерации синетических данных:
   - Для типа int: Начальное значение (`initial_value`), количество повторений (repeat_number) и с каким шагом (step). 
       * Например, `initial_value=5`, `repeat_number=2`, `step=5` будет генерировать последовательность `[5, 5, 10, 10, 15, 15, ...]`
   - Для типа `int`: без указания правил будет генерировать случайное целочисленное значение в интервале минимум и максимум значения типа int
   - Для типа `int`: нижняя (`lower_threshold`) и верхняя граница (`upper_threshold`) будет генерировать случайное целочисленное значение в заданном интервале значений
   - Для всех поддерживаемых типов данных: возможность задания списка значений, из которых случайным образом будет сформировано список
   - Для всех поддерживаемых типов данных: возможность указания списка значений для внесения их в колонку таблицы в том же порядке. 

`(!)` Только длина списка должна совпадать с параметром количество строк таблицы `row_count`
- Для типа `bool`: без указания правил будет генерировать `true` или `false`
- Для типа `timestamp`: без указания правил будет генерировать случайный timestamp в интервале год назад по текущее время
- Для типа `string`: без указания правил будет генерировать случайную строку с длиной 4 символа
- Для типа `varchar`: без указания правил будет генерировать случайную строку с длиной, указанная в параметре `varchar(х)`

## Инструкция для настройки и запуска приложения
1. Подготовить правила создания схем, таблиц, загрузка полу-синтетических данных и правил к ним (пример `rulesPostgresExample.json`, `rulesGreenplumExampleSemiSynthetic.json`)
2. В application.properties указать параметры:
   - `kafka.brokers.src` - список хостов с портами источника Kafka (Пример: `kafka.brokers.src=localhost:9092`)
   - `kafka.brokers.target` - список хостов с портами целевой Kafka (Пример: `kafka.brokers.src=localhost:9093`)
   - `connection.url.src` - строка подключения СУБД источника (Пример: `jdbc:postgresql://localhost:5433/postgres`)
   - `connection.url.target` - строка подключения целевой СУБД (Пример: `jdbc:hive2://localhost:10000/default`)
   - `driver.class.name.src` - название драйвера СУБД источника (Пример: `org.postgresql.Driver`)
   - `driver.class.name.target` - название драйвера СУБД источника (Пример: `org.apache.hive.jdbc.HiveDriver`)
   - `user.src` - пользователь для подключения к СУБД источника
   - `user.target` - пользователь для подключения к целевому СУБД
   - `password.src` - пароль для подключения к СУБД источника
   - `password.target` - пароль для подключения к целевому СУБД
   - `synthetic.rules.path` - путь до файла с правилами
3. Запуск приложения с аргументами:
   - указания режима приложения (`--mode` или `-m`): батчевый (`batch`) или потоковый (`stream`)
   - указания тип генерации (`--genType` или `-g`): синтетический (`synthetic`) или полу-синтетический (`semi-synthetic`)
   - указания источника (`--srcDataType` или `-s`): СУБД (`postgres`, `hive`, `greenplum`) или Kafka (`kafka`)
   - указания целевой системы (`--targetDataType` или `-t`): СУБД (`postgres`, `hive`, `greenplum`) или Kafka (`kafka`)
   * Примеры:
     - `-m batch -g synthetic -t postgres`
     - `-m batch -g synthetic -t kafka`
     - `-m batch -g semi-synthetic -s postgres -t postgres`
     - `-m batch -g semi-synthetic -s postgres -t greenplum`
     - `-m batch -g semi-synthetic -s postgres -t kafka`
     - `-m stream -g semi-synthetic -s kafka -t kafka`

Примечение:
* `(!)` Все аргументы обязательные, кроме случая синтетической генерации, где можно не указывать аргумент (`--srcDataType` или `-s`)

## Файл с правилами создания схем, таблиц и генерации данных
1. `schema` - название схемы в БД
2. `tables` - список таблиц в схеме БД
   - `name` - название таблицы
   - `kafka_topic` - название топика Kafka. При не указании парамтеры название топика будет установлено schema.table_name
   - `source_db_query` - запрос в СУБД для получения исходных данных для генерации данных их на основе (`Release 1.0.0`)
   - `row_count` - количество строк в таблице
   - `recreate` - флаг, определяющий пересоздание таблицы. В случае false в текущую таблицу (если существует) данные добавляются к уже имеющимся.
   - `columns` - поля таблицы
     * `name` - название поля
     * `type` - тип данных поля
     * `nullable` - флаг, определяющий параметр может ли поле заполнять null при создании таблицы (поддерживается для Postgres)
     * `unique` - флаг, определяющий параметр должно ли значение поля быть уникальным при создании таблицы (поддерживается для Postgres)
     * `isPrimaryKey` - флаг, определяющий поле является ключом таблицы при создании таблицы (поддерживается для Postgres)
     * `data` - правило заполения значений поля
       - `action` - тип заполнения: генерация (generation) и добавление указанных данных (insert)
       - `rules` - правила генерации значений поля
         * `lower_threshold` - нижняя граница (поддерживается для типа данных int и timestamp)
         * `upper_threshold` - верхняя граница (поддерживается для типа данных int и timestamp)
         * `initial_value` - начальное значение (поддерживается для типа данных int)
         * `repeat_number` - количество повторений (поддерживается для типа данных int)
         * `step` - шаг последовательности (поддерживается для типа данных int)
         * `acceptable_values` - допустимые значения
         * `values` - значения для добавления при типе заполнения insert
         * `start` - начало времени (поддерживается для типа данных timestamp)
         * `step_by_minutes` - шаг генерации даты, в минутах
         * `src_column` - название колонки в СУБД, полученный в результате запроса `source_db_query`, которая будет вставлена в текущую поле таблицы в целевом СУБД (`Release 1.0.0`)
3. `stream` - секция с параметрами для потокового режима (`Release 1.0.0`)
    - `kafka.src.topics` - список Kafka топиков в качестве источника
    - `kafka.target.topic` - название целевого Kafka топика

## Сборка приложения FAT JAR
`sbt assembly`

## Запуск приложения FAT JAR
`java -jar file_app_name.jar app_args`

где 

`file_app_name` - название файла с расширением jar

`app_args` - аргументы приложения

Примеры:
- `java -jar .\syntheticdatagenerator-assembly-1.0.0.jar --mode batch --genType synthetic --targetDataType postgres`
- `java -jar .\syntheticdatagenerator-assembly-1.0.0.jar --mode batch --genType synthetic --targetDataType kafka`
- `java -jar .\syntheticdatagenerator-assembly-1.0.0.jar --mode batch --genType semi-synthetic --srcDataType postgres --targetDataType postgres`
- `java -jar .\syntheticdatagenerator-assembly-1.0.0.jar --mode batch --genType semi-synthetic --srcDataType postgres --targetDataType greenplum`
- `java -jar .\syntheticdatagenerator-assembly-1.0.0.jar --mode batch --genType semi-synthetic --srcDataType postgres --targetDataType kafka`
- `java -jar .\syntheticdatagenerator-assembly-1.0.0.jar --mode stream --genType semi-synthetic --srcDataType kafka --targetDataType kafka`

А также возможно запуск с кратким названием параметров:

- `java -jar .\syntheticdatagenerator-assembly-1.0.0.jar -m batch -g synthetic -t postgres`
- `java -jar .\syntheticdatagenerator-assembly-1.0.0.jar -m batch -g synthetic -t kafka`
- `java -jar .\syntheticdatagenerator-assembly-1.0.0.jar -m batch -g semi-synthetic -s postgres -t postgres`
- `java -jar .\syntheticdatagenerator-assembly-1.0.0.jar -m batch -g semi-synthetic -s postgres -t greenplum`
- `java -jar .\syntheticdatagenerator-assembly-1.0.0.jar -m batch -g semi-synthetic -s postgres -t kafka`
- `java -jar .\syntheticdatagenerator-assembly-1.0.0.jar -m stream -g semi-synthetic -s kafka -t kafka`

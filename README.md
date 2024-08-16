# SyntheticDataGenerator

## Поддерживаемый функционал приложения
1. Генерация синтетических данных согласно указанным правилам (Пример правил ./rulesPostgresExample.json)
2. Возможность создания множество целых схем со своими таблицами с синетическими данными в Postgres и Hive
3. Возможность записи синететических данных в формате JSON в Kafka Topic в потоковом режиме с помощью Flink 
4. Поддержка генерация следующих типов данных: int, string (только для Hive), varchar, boolean, timestamp
5. Поддерживаемык правила генерации синетических данных:
   5.1. Для типа int: Начальное значение (initial_value), количество повторений (repeat_number) и с каким шагом (step)
      Например, initial_value=5, repeat_number=2, step=5 будет генерировать последовательность [5, 5, 10, 10, 15, 15, ...]
   5.2. Для типа int: без указания правил будет генерировать случайное целочисленное значение в интервале минимум и максимум значения типа int
   5.3. Для типа int: нижняя (lower_threshold) и верхняя граница (upper_threshold) будет генерировать случайное целочисленное значение в заданном интервале значений
   5.4. Для всех поддерживаемых типов данных: возможность задания списка значений, из которых случайным образом будет сформировано список
   5.5. Для всех поддерживаемых типов данных: возможность указания списка значений для внесения их в колонку таблицы в том же порядке
   (!) Только длина списка должна совпадать с параметром количество строк таблицы row_count
   5.6. Для типа bool: без указания правил будет генерировать true или false
   5.7. Для типа timestamp: без указания правил будет генерировать случайный timestamp в интервале год назад по текущее время
   5.8. Для типа string: без указания правил будет генерировать случайную строку с длиной 4 символа
   5.9. Для типа varchar: без указания правил будет генерировать случайную строку с длиной, указанная в параметре varchar(х)

## Инструкция для настройки и запуска приложения
1. Подготовить правила создания схем и таблиц (пример rulesHiveExample.json и rulesPostgresExample.json)
2. В application.properties указать параметры:
   2.1. kafka.brokers (список хостов с портами Kafka)
   2.2. connection.url (строка подключения БД)
   2.3. driver.class.name (название драйвера БД)
   2.4. user (логин) и password (пароль) для подключения к БД
   2.5. synthetic.rules.path (путь до файла с правилами создания схем и таблиц с правилами генерации данных)
3. Запуск приложения с аргументами
   3.1. указания режима приложения: батчевый (batch) или потоковый (stream). Пример: -m batch или -m stream
   3.2. указания целевой БД: postgres или hive. Пример: -m batch -t postgres или -m batch -t hive
   (!) При задании батчевого режима обязательно нужно задать целевой БД.
   (!) А при задании потокового нет необходимости так как поток будет писать в Kafka.

## Файл с правилами создания схем и таблиц
1. schema - название схемы в БД
2. tables - список таблиц в схеме БД
   2.1. name - название таблицы
   2.2. kafka_topic - название топика Kafka. При не указании парамтеры название топика будет установлено schema.table_name
   2.3. row_count - количество строк в таблице
   2.4. recreate - флаг, определяющий пересоздание таблицы. В случае false в текущую таблицу (если существует) данные добавляются к уже имеющимся.
   2.5. columns - поля таблицы
      2.5.1. name - название поля
      2.5.2. type - тип данных поля
      2.5.3. nullable - флаг, определяющий параметр может ли поле заполнять null при создании таблицы (поддерживается для Postgres)
      2.5.4. unique - флаг, определяющий параметр должно ли значение поля быть уникальным при создании таблицы (поддерживается для Postgres)
      2.5.5. isPrimaryKey - флаг, определяющий поле является ключом таблицы при создании таблицы (поддерживается для Postgres)
      2.5.6. data - правило заполения значений поля
         2.5.6.1. action - тип заполнения: генерация (generation) и добавление указанных данных (insert)
         2.5.6.2. rules - правила генерации значений поля
            2.5.6.2.1. lower_threshold - нижняя граница (поддерживается для типа данных int и timestamp)
            2.5.6.2.2. upper_threshold - верхняя граница (поддерживается для типа данных int и timestamp)
            2.5.6.2.3. initial_value - начальное значение (поддерживается для типа данных int)
            2.5.6.2.4. repeat_number - количество повторений (поддерживается для типа данных int)
            2.5.6.2.5. step - шаг последовательности (поддерживается для типа данных int)
            2.5.6.2.6. acceptable_values - допустимые значения
            2.5.6.2.7. values - значения для добавления при типе заполнения insert
            2.5.6.2.8. start - начало времени (поддерживается для типа данных timestamp)
            2.5.6.2.9. step_by_minutes - шаг генерации даты, в минутах


## Инструкция по запуску приложения генерации синтетических данных

1. Настройка 
2. Поднятие докер образа с Hive (например, https://github.com/big-data-europe/docker-hive/tree/master)
2. Поднятие докер образа с Postgres (например, https://github.com/khezen/compose-postgres/tree/master)
3. Настройка параметров генерации схем и таблиц (пример, в корневой директории rulesMinHiveTest.json и rulesMinPostgresTest.json)
4. Настройка параметров в application.properties
5. Запуск Main.scala с аргументом -s (или --srcDataType) Hive (или Postgres)
6. При успешной работе должны создаться схемы и таблицы в БД, указанном в application.properties
   Если таблицы существуют перед запуском, то они удаляются и пересоздаются со структурой, которая указано в файле из п.3


## Plan (draft version)

# Краткосрочный
1. Параметр потоковой или статический (genType)
2. Синтетический генератор потоковых данных (создаем несколько файлов)

# Логика генерации
В исходном файле кроме описания полей таблиц также описываются правила генерации, а именно:
- верхний и нижний пороги данных(для числовых типов данных и для дат)
- частота генерации(начальное значение, количество на каждое значение, шаг ), массив для выбора значений.
Т.е. данные будут генерироваться либо произвольно(для столбцов, которых нет в rules), либо по правилам: 
- дискретные значения в интервале(пример для дат: каждая дата больше предыдущей на 15 минут) 
- или из списка(когда нужно использовать только указанные значения)

# Общие
1. Определить форматы входных данных (источник)\
   1.1. чисто синтетические данные (json template)\
   1.2. таблица Postgres (частично синтетические данные)
2. Определить форматы выходных данных\
   2.1. Hive tables\
   2.2. Postgres tables (на основании таблицы Postgres)\
   Postgres -> Postgres

   (?)(PARQUET, AVRO)
3. (?) Kafka -> Flink\
   источник -> приложение -> выходные данные
4. Предусмотреть возможность создания нескольких таблиц в параллельном режиме
5. Разделение потока источника по функции на 2 потока
6. Собрать в один поток пару потоков источника
7. Работа с AVRO (свои CDC-механизмы)
8. Изменение схемы данных (работа с полями, разделение объединение потоков)
9. Добавление данных (insert в диапазоне ключей (с такого по такой ключ))
10. Умножение данных

# Потоковый данные 
1. Партицинированная Кафка как источник, сохранение партиции на выходе флинка (управление записью в партиции на выходе в кафку)
2. Грейсфул шаутдаун генератора данных (всего приложения (закрытие всех кафка коннектов))
3. Синтезатор данных как балансировщик потоковых данных (смотрим на оффсеты входных кафка источников, равномерно или по функции распределяем в выходные кафки)

# Статические данные


Создать на основе cats effects (ioapp) приложение для генерации синтетических тестовых данных, работающая в нескольких режимах:
1. чисто синтетические данные
2. частично синтетические данные

Предусмотреть выбор типов входных данных (Postgres, json template)
Предусмотреть выбор типов выходных данных (Postgres, Hive)

Для парсинга входных параметров консольного приложения использовать библиотеки scopt, monocle

## Инструкция по тестированию приложения генерации синтетических данных

1. Поднятие докер образа с Hive (например, https://github.com/big-data-europe/docker-hive/tree/master)
2. Поднятие докер образа с Postgres (например, https://github.com/khezen/compose-postgres/tree/master)
3. Настройка параметров генерации схем и таблиц (пример, в корневой директории rulesMinHiveTest.json и rulesMinPostgresTest.json)
4. Настройка параметров в application.properties
5. Запуск Main.scala с аргументом -s (или --srcDataType) Hive (или Postgres)
6. При успешной работе должны создаться схемы и таблицы в БД, указанном в application.properties
   Если таблицы существуют перед запуском, то они удаляются и пересоздаются со структурой, которая указано в файле из п.3

## Getting started

To make it easy for you to get started with GitLab, here's a list of recommended next steps.

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!

## Add your files

- [ ] [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
- [ ] [Add files using the command line](https://docs.gitlab.com/ee/gitlab-basics/add-file.html#add-a-file-using-the-command-line) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://gitlab.akb-it.ru/ostrashevskiy/syntheticdatagenerator.git
git branch -M main
git push -uf origin main
```

## Integrate with your tools

- [ ] [Set up project integrations](https://gitlab.akb-it.ru/ostrashevskiy/syntheticdatagenerator/-/settings/integrations)

## Collaborate with your team

- [ ] [Invite team members and collaborators](https://docs.gitlab.com/ee/user/project/members/)
- [ ] [Create a new merge request](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)
- [ ] [Automatically close issues from merge requests](https://docs.gitlab.com/ee/user/project/issues/managing_issues.html#closing-issues-automatically)
- [ ] [Enable merge request approvals](https://docs.gitlab.com/ee/user/project/merge_requests/approvals/)
- [ ] [Automatically merge when pipeline succeeds](https://docs.gitlab.com/ee/user/project/merge_requests/merge_when_pipeline_succeeds.html)

## Test and Deploy

Use the built-in continuous integration in GitLab.

- [ ] [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/index.html)
- [ ] [Analyze your code for known vulnerabilities with Static Application Security Testing(SAST)](https://docs.gitlab.com/ee/user/application_security/sast/)
- [ ] [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/ee/topics/autodevops/requirements.html)
- [ ] [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/ee/user/clusters/agent/)
- [ ] [Set up protected environments](https://docs.gitlab.com/ee/ci/environments/protected_environments.html)

***

# Editing this README

When you're ready to make this README your own, just edit this file and use the handy template below (or feel free to structure it however you want - this is just a starting point!). Thank you to [makeareadme.com](https://www.makeareadme.com/) for this template.

## Suggestions for a good README
Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.

## Name
Choose a self-explaining name for your project.

## Description
Let people know what your project can do specifically. Provide context and add a link to any reference visitors might be unfamiliar with. A list of Features or a Background subsection can also be added here. If there are alternatives to your project, this is a good place to list differentiating factors.

## Badges
On some READMEs, you may see small images that convey metadata, such as whether or not all the tests are passing for the project. You can use Shields to add some to your README. Many services also have instructions for adding a badge.

## Visuals
Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method.

## Installation
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.

## Usage
Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README.

## Support
Tell people where they can go to for help. It can be any combination of an issue tracker, a chat room, an email address, etc.

## Roadmap
If you have ideas for releases in the future, it is a good idea to list them in the README.

## Contributing
State if you are open to contributions and what your requirements are for accepting them.

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.

## Authors and acknowledgment
Show your appreciation to those who have contributed to the project.

## License
For open source projects, say how it is licensed.

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.

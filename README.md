# Stacker

## Cloud Native Hierarchical Flow Machine.

[Презентация](./conception/presentation3.pdf)

## Назначение
Библиотека обеспечивает инфраструктуру и логику построения 
разветвлённых фронтальных сценариев **(screen flow)**
из отдельных, независимых компонентов.

Применима для мобильных приложений, Web приложений, АРМ и т. д.

## Проблема
Большинство современных приложений, взаимодействующих с пользователем через UI 
представляют из себя набор взаимосвязанных экранных форм и могут быть описаны 
как фронтальный сценарий.

Разработка горизонтально масштабируемых под высокую нагрузку приложений, 
реализующих сложные фронтальные
сценарии представляет собой и техническую и управленческую проблему.

С технической точки зрения мы имеем 
- Громоздкий монолит или набор громоздких монолитов на серверной стороне 
и достаточно простую клиентскую часть.
- Или более современную альтернативу - множество независимых микросервисов 
на серверной стороне и горомоздкий монолит на клиентской стороне.

(Либо куча в коде и сессии, либо кучки кода и куча сессий)
    
В первом случае - проблемы масштабируемости и длительный релизный цикл, 
во втором - проблемы с управлением взаимосвязями и состоянием и необходимость содержать 
несколько фронтов, функциональность которых может сильно различаться из-за разной 
скорости разработки.

В любом случае количество взаимосвязей в системе растёт гораздо быстрее чем развивается 
функциональность, что в конечном итоге приводит к потере гибкости, управляемости и 
способности к дальнейшему развитию.

В настоящий момент на рынке нет решений, позволяющих строить горизонтально масштабируемые 
системы способные к расширению функциональности без потери управляемости.

## Решение
**Stacker** выносит функцию управления взаимосвязями и состоянием в отдельный слой, 
оставляя реализацию бизнес-логики на уровне микросервисов без состояния.

Система на основе Stacker разделяется на три слоя
- макро уровень - уровень канала обслуживания (мобильное приложение, Web приложение, 
АРМ менеджера, АРМ операциониста и т. д. ), технически [Router](./stacker-router/)
- бизнес уровень - уровень реализации бизнес логики компонентов процесса [Flow](./stacker-flow/)
- сервисный уровень - уровень баз данных и бэк сервисов

## Процесс разработки 

Процесс разработки может быть разбит на три части, для каждой из которых условно можно выделить роль:

### Бизнес аналитик
разработка фронтального сценария - БТ и схемы всего процесса и/или его частей - компонентов

### Разработчик
реализация бизнес логики компонентов фронтального сценария:

- Декомпозиция фронтального сценария (flow) 
  на компоненты (subflow) - сценарии, выполняющие 
  одну функцию, имеющие аргумент и возвращаемое значение.
- Разработка компонентов
    - разработка точек взаимодействия с клиентом, наследник 
    [StateQuestion](./stacker-flow/src/main/java/io/github/krieven/stacker/flow/StateQuestion.java) 
    - разработка точек передачи управления в другие компоненты (вызов subflow), насдедник
    [StateOuterCall](./stacker-flow/src/main/java/io/github/krieven/stacker/flow/StateOuterCall.java) 
    - реализация схемы и контракта компонента фронтального сценария, наследник
    [BaseFlow](./stacker-flow/src/main/java/io/github/krieven/stacker/flow/BaseFlow.java) 

### Администратор приложения
Сборка и управление общей схемой приложения на макро уровне 
- регистрация компонентов фронтального сценария в приложении, 
- установка взаимосвязей между компонентами

(планируется разработка инструментария)
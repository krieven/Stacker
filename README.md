# Stacker

## Cloud Native Hierarchical Flow Machine.

<a href="conception/presentation3.pdf">
Презентация
</a>

## Назначение
Библиотека обеспечивает инфраструктуру и логику построения разветвлённых фронтальных сценариев (screen flow)
из отдельных, независимых компонентов.

## Проблема
Большинство современных приложений, взаимодействующих с пользователем через UI представляют из себя
набор взаимосвязанных экранных форм и могут быть описаны как фронтальный сценарий.

Расширение функциональности в таких приложениях приводит к экспоненциальному усложнению системы. 
Способность к измененииям падает, этими изменениями становится невзможно управлять.

## Решение
Stacker позволяет строить горизонтально масштабируемые системы способные к 
расширению функциональности без потери управляемости.

Это становится возможным за счёт вынесения функции управления взаимосвязями в отдельный слой.

### Роль - бизнес аналитик
разработка фронтального сценария

### Роль - разработчик
реализация бизнес логики фронтального сценария:

- Декомпозиция фронтального сценария (flow) 
  на компоненты (subflow) - сценарии выполняющие 
  одну функцию (в терминах BPMN - role)
- Разработка компонентов
    - разработка точек взаимодействия с клиентом [StateQuestion](./stacker-flow/src/main/java/io/github/krieven/stacker/flow/StateOuterCall.java) 
    - разработка точек передачи управления в другие компоненты (вызов subflow) StateOuterCall
    - реализация схемы и контракта фронтального сценария BaseFlow

### Роль - администратор приложения
регистрация фронтального сценария в системе, подключение к другим компонентам
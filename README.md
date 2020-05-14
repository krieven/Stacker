# Stacker CloudBunch

## Идея
### CloudBunch - иерархическая *State machine* в которой каждый слой иерархии находится в отдельном микросервисе.

Router хранит стек открытых в контексте сессии клиента сервисов вместе с их текущими состояниями. На верху стека находится активный в данный момент сервис.

Все запросы от клиента поступают на Router. Router перенаправляет их на активный сервис.

Открытие (вызов) нового слоя осуществляется ответом от сервиса роутеру с указанием имени (ключа) хендлера обратного вызова. Когда активный сервис закрывается, его результат перенаправляется опять-же через роутер на вызывающий сервис. 

#### Схема прохождения обычного запроса

<img src="schema1.png">
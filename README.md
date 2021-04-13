# CloudBunch

## Идея - Cloud Ready иерархическая *State machine*.

Многие интерактивные приложения это 
определенная, часто - разветвленная последовательность 
вопросов и ответов, 
причем вопросы задает приложение, а отвечает на них 
клиент. Для описания подобных последовательностей
удобно и целесообразно использовать *State machine* (SM), 
для больших и разветвленных - декомпозированную иерархическую 
*State machine* (HSM). Иногда их также называют *Flow machine*.

Раз уже речь зашла о больших и разветвленных приложениях и их 
декомпозиции, имеет смысл говорить и о декомпозиции процесса 
разработки. Было бы неплохо раздать командам разработчиков 
отдельные фичи нашего приложения и позволить им выпускать релизы по
готовности, без ПИРов и фризов. Тоесть разбив большое и сложное
приложение на маленькие и простые <b>микросервисы</b>...

Как известно, микросервисы очень хороши когда они не хранят в себе 
состояния (сессии), слабо связаны и общаются между собой в соответствии с 
формальным контрактом. Тогда система становится горизонтально 
масштабируемой, детерминированной, управляемой и надежной, сразу ко всем 
приходит неизбежное счастье.

Оно всем очень нужно - это счастье, но существует ужасное противоречие -
с одной стороны <b>Stateless</b> microservice, с другой стороны <b>State</b> machine...
Сосояние необходимо, но *его не должно быть*!

### Как поместить State machine в Stateless microsrevice?
Первое, что приходит в голову - пусть каждый хранит свою сессию на диске или в базе данных. Вроде решение,
но если честно - решение так себе: - каждый сервис обязан следить за своими сессиями, сохранять,
обновлять, удалять. Размазанное во времени и пространстве состояние - это та еще головная боль,
часто просто необходимо централизованное управление всем этим чудовищем.

Второй, - широко используемый вариант - централизованное хранилище 


### Как это может быть организовано?


### CloudBunch - мир будущего

Router хранит стек открытых в контексте сессии 
клиента сервисов вместе с их текущими состояниями. 
На верху стека находится активный в данный момент 
сервис.

Все запросы от клиента поступают на Router. Router перенаправляет их на активный сервис.

Открытие (вызов) нового слоя осуществляется ответом от сервиса роутеру (тип OPEN). 
Когда активный сервис закрывается, его результат перенаправляется опять-же через роутер на вызывающий сервис. 

#### Схема прохождения обычного запроса

<img src="schema1.png">

В первом варианте - сессия и состояние отсутствуют, открывапется первый слой , сервис по умолчанию.

Во втором варианте состояние присутствует, запрос направляется в активный сервис

### Схема взаимодействия между сервисами (открытие нового слоя, возврат на предыдущий слой)

<img src="schema2.png">
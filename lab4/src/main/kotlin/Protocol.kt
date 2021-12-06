// Роль узла в топологии связей узлов в пределах игры
enum NodeRole {
    NORMAL = 0; // Обычный узел, лист в топологии "звезда"
    MASTER = 1; // Главный узел, центр в топологии "звезда"
    DEPUTY = 2; // Заместитель главного узла
    VIEWER = 3; // Наблюдатель, похож на NORMAL, но не имеет змеи в статусе ALIVE, только получает обновления статуса
}

// Тип игрока
enum PlayerType {
    HUMAN = 0; // Живой пользователь
    ROBOT = 1;  // Робот, управляет своей змеёй с помощью алгоритма (вне задачи, для желающих)
}

// Игрок
message GamePlayer {
    required string name = 1;       // Имя игрока (для отображения в интерфейсе)
    required int32 id = 2;          // Уникальный идентификатор игрока в пределах игры
    required string ip_address = 3; // IPv4 или IPv6 адрес игрока в виде строки (отправитель не знает свой IP, поэтому указывает тут пустую строку)
    required int32 port = 4;        // Порт UDP-сокета игрока
    required NodeRole role = 5;     // Роль узла в топологии
    optional PlayerType type = 6 [default = HUMAN]; // Тип игрока
    required int32 score = 7;       // Число очков, которые набрал игрок
}

/* Параметры идущей игры (не должны меняться в процессе игры) */
message GameConfig {
    optional int32 width = 1 [default = 40];           // Ширина поля в клетках (от 10 до 100)
    optional int32 height = 2 [default = 30];          // Высота поля в клетках (от 10 до 100)
    optional int32 food_static = 3 [default = 1];      // Количество клеток с едой, независимо от числа игроков (от 0 до 100)
    optional float food_per_player = 4 [default = 1];  // Количество клеток с едой, на каждого игрока (вещественный коэффициент от 0 до 100)
    optional int32 state_delay_ms = 5 [default = 1000]; // Задержка между ходами (сменой состояний) в игре, в миллисекундах (от 1 до 10000)
    optional float dead_food_prob = 6 [default = 0.1]; // Вероятность превращения мёртвой клетки в еду (от 0 до 1).
    optional int32 ping_delay_ms = 7 [default = 100];   // Задержка между отправкой ping-сообщений, в миллисекундах (от 1 до 10000)
    optional int32 node_timeout_ms = 8 [default = 800]; // Таймаут, после которого считаем что узел-сосед отпал, в миллисекундах (от 1 до 10000)
}

/* Игроки конкретной игры */
message GamePlayers {
    repeated GamePlayer players = 1; // Список всех игроков
}

enum Direction {
    UP = 1;     // Вверх (в отрицательном направлении оси y)
    DOWN = 2;   // Вниз (в положительном направлении оси y)
    LEFT = 3;   // Влево (в отрицательном направлении оси x)
    RIGHT = 4;  // Вправо (в положительном направлении оси x)
}

/* Текущее состояние игрового поля */
message GameState {
    /* Координаты в пределах игрового поля, либо относительное смещение координат.
     * Левая верхняя клетка поля имеет координаты (x=0, y=0).
     * Направление смещения задаётся знаком чисел. */
    message Coord {
        optional sint32 x = 1 [default = 0]; // По горизонтальной оси, положительное направление - вправо
        optional sint32 y = 2 [default = 0]; // По вертикальной оси, положительное направление - вниз
    }
    // Змея
    message Snake {
        // Статус змеи в игре
        enum SnakeState {
            ALIVE = 0;  // Змея управляется игроком
            ZOMBIE = 1; // Змея принадлежала игроку, который вышел из игры, она продолжает движение куда глаза глядят
        }
        required int32 player_id = 1; // Идентификатор игрока-владельца змеи, см. GamePlayer.id
        /* Список "ключевых" точек змеи. Первая точка хранит координаты головы змеи.
         * Каждая следующая - смещение следующей "ключевой" точки относительно предыдущей,
         * в частности последняя точка хранит смещение хвоста змеи относительно предыдущей "ключевой" точки. */
        repeated Coord points = 2;
        required SnakeState state = 3 [default = ALIVE]; // статус змеи в игре
        required Direction head_direction = 4; // Направление, в котором повёрнута голова змейки в текущий момент
    }
    required int32 state_order = 1;   // Порядковый номер состояния, уникален в пределах игры, монотонно возрастает
    repeated Snake snakes = 2;        // Список змей
    repeated Coord foods = 3;         // Список клеток с едой
    required GamePlayers players = 4; // Актуальнейший список игроков
    required GameConfig config = 5;   // Параметры игры
}

// Общий формат любого сообщения
message GameMessage {
    // Ничего не меняем, просто говорим что мы живы с интервалом ping_delay_ms
    message PingMsg {
    }
    // Не-центральный игрок просит повернуть голову змеи
    message SteerMsg {
        required Direction direction = 1; // Куда повернуть на следующем шаге
    }
    // Подтверждение сообщения с таким же seq
    message AckMsg {
    }
    // Центральный узел сообщает отсальным игрокам состояние игры
    message StateMsg {
        required GameState state = 1; // Состояние игрового поля
    }
    // Уведомление об идущей игре, регулярно отправляется multicast-ом
    message AnnouncementMsg {
        required GamePlayers players = 1;            // Текущие игроки
        required GameConfig config = 2;              // Параметры игры
        optional bool can_join = 3 [default = true]; // Можно ли новому игроку присоединиться к игре (есть ли место на поле)
    }
    // Новый игрок хочет присоединиться к идущей игре
    message JoinMsg {
        optional PlayerType player_type = 1 [default = HUMAN]; // Тип присоединяющегося игрока
        optional bool only_view = 2 [default = false]; // Если хотим только понаблюдать, но не играть
        required string name = 3; // Имя игрока
    }
    // Ошибка операции (например отказ в присоединении к игре, т.к. нет места на поле)
    message ErrorMsg {
        required string error_message = 1; // Строковое сообщение, нужно отобразить его на экране, не блокируя работу программы
    }
    /* Сообщение о смене роли:
     * 1. от заместителя другим игрокам о том, что пора начинать считать его главным (sender_role = MASTER)
     * 2. от осознанно выходящего игрока (sender_role = VIEWER)
     * 3. от главного к умершему игроку (receiver_role = VIEWER)
     * 4. в комбинации с 1,2 или отдельно от них: назначение кого-то заместителем (receiver_role = DEPUTY)
     * 5. в комбинации с 2 от главного узла заместителю о том, что он становится главным (receiver_role = MASTER)
     */
    message RoleChangeMsg {
        optional NodeRole sender_role = 1;
        optional NodeRole receiver_role = 2;
    }
    required int64 msg_seq = 1;   // Порядковый номер сообщения, уникален для отправителя в пределах игры, монотонно возрастает
    optional int32 sender_id = 10;   // ID игрока-отправителя этого сообщения (обязательно для AckMsg и RoleChangeMsg)
    optional int32 receiver_id = 11; // ID игрока-получателя этого сообщения (обязательно для AckMsg и RoleChangeMsg)
    // Тип сообщения
    oneof Type {
        PingMsg ping = 2;
        SteerMsg steer = 3;
        AckMsg ack = 4;
        StateMsg state = 5;
        AnnouncementMsg announcement = 6;
        JoinMsg join = 7;
        ErrorMsg error = 8;
        RoleChangeMsg role_change = 9;
    }
}

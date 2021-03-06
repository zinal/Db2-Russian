# Параметры конфигурации Db2

Конфигурация сервера Db2 может быть задана на четырех различных уровнях:
* переменные среды;
* реестр профиля Db2;
* файл конфигурации менеджера баз данных (DBM CFG);
* файл конфигурации базы данных (DB CFG).


Переменные среды задаются на уровне операционной системы
сервера и средствами операционной системы. Для ОС Windows эти
переменные фактически являются глобальными для сервера, для
ОС семейств Unix и Linux для каждого экземпляра могут быть
установлены свои специфические настройки переменных среды.

Параметры реестра профиля DB2 могут
быть заданы на уровне операционной системы (глобально) либо на уровне
экземпляра, при этом настройки на уровне экземпляра
переопределяют значения, определённые на уровне
операционной системы. Просмотр и установка значений параметров реестра профиля 
DB2 выполняется с помощью команды db2set.

Параметры файла конфигурации менеджера баз данных
определяются на уровне экземпляра, а параметры
конфигурации базы данных – на уровне базы данных.

Многие параметры являются динамическими, т. е. внесенные
изменения сразу же вступают в силу; однако есть параметры, для изменения
которых необходимо остановить и снова запустить экземпляр. Это можно сделать в
командной строке с помощью команд `db2stop` и `db2start`. Перед остановкой
экземпляра все приложения должны отключиться. Для принудительной остановки
экземпляра с отключением всех приложений можно воспользоваться
командой `db2stop force`.

Файл конфигурации менеджера баз данных включает параметры,
влияющие на экземпляр и все базы данных, содержащихся в нем. Файл конфигурации
менеджера базы данных можно просмотреть или изменить с помощью командной строки
(командами `GET DBM CFG` и `UPDATE DBM CFG`), а также средствами 
[IBM Data Studio](https://www.ibm.com/developerworks/downloads/im/data/).

Файл конфигурации базы данных включает параметры, влияющие
на определенную базу данных. Файл конфигурации базы данных можно просмотреть
или изменить с помощью командной строки (командами `GET DB CFG` и
`UPDATE DB CFG`), а также средствами IBM Data Studio.

Детальное описание поддерживаемых
[переменных среды и реестра профиля DB2](http://www.ibm.com/support/knowledgecenter/SSEPGG_11.5.0/com.ibm.db2.luw.admin.regvars.doc/doc/c0007340.html),
а также
[параметров конфигурации](http://www.ibm.com/support/knowledgecenter/SSEPGG_11.5.0/com.ibm.db2.luw.admin.config.doc/doc/c0004555.html)
приведено в официальной документации Db2.

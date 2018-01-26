/*
   (с) 2013 ООО "КУРС-ИТ"  

   Этот файл — часть КУРС:Celesta.
   
   КУРС:Celesta — свободная программа: вы можете перераспространять ее и/или изменять
   ее на условиях Стандартной общественной лицензии GNU в том виде, в каком
   она была опубликована Фондом свободного программного обеспечения; либо
   версии 3 лицензии, либо (по вашему выбору) любой более поздней версии.

   Эта программа распространяется в надежде, что она будет полезной,
   но БЕЗО ВСЯКИХ ГАРАНТИЙ; даже без неявной гарантии ТОВАРНОГО ВИДА
   или ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Подробнее см. в Стандартной
   общественной лицензии GNU.

   Вы должны были получить копию Стандартной общественной лицензии GNU
   вместе с этой программой. Если это не так, см. http://www.gnu.org/licenses/.

   
   Copyright 2013, COURSE-IT Ltd.

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see http://www.gnu.org/licenses/.

 */

/**Celesta system schema. Not for modification.*/
create SCHEMA bass version '1.0';

/**Active schemas list.*/
create table schemas(
  /**schema prefix (id)*/
  id varchar(30) not null primary key, 
  /**schema version tag*/
  version varchar(2000) not null,
  /**schema creation script length in bytes*/
  length int not null,
  /**schema creation script CRC32 value*/
  checksum varchar(8) not null,
  /**schema status
   {option: [ready, upgrading, error, recover, lock]}*/  
  state int not null default 3,
  /**date and time of last schema status update*/
  lastmodified datetime not null default getdate(), 
  /**comment (e. g. error message for the last failed auto-update)*/
  message text not null default '' 
) with no version check;
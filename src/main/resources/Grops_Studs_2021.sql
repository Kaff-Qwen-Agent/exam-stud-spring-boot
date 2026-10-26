CREATE TABLE IF NOT EXISTS gruppyi (
  Nazvanie VARCHAR(50) NOT NULL UNIQUE,
  Shifr INT NOT NULL AUTO_INCREMENT,
  DataFormir DATE NOT NULL,
  KodPlana INT NOT NULL,
  Status VARCHAR(50),
  StatusDate DATE,
  PRIMARY KEY (Shifr)
);

CREATE TABLE IF NOT EXISTS studentyi (
  ShifrGruppyi INT NOT NULL,
  NomerZachetki BIGINT NOT NULL,
  Familiya VARCHAR(30) NOT NULL,
  Imya VARCHAR(30) NOT NULL,
  Otchestvo VARCHAR(30) NOT NULL,
  Gorod VARCHAR(30) NOT NULL,
  Adres VARCHAR(80) NOT NULL,
  Tel VARCHAR(30) NOT NULL,
  Status VARCHAR(50),
  StatusDate DATE,
  PRIMARY KEY (NomerZachetki),
  CONSTRAINT FK_Studentyi_Gruppyi_ FOREIGN KEY (ShifrGruppyi) REFERENCES gruppyi (Shifr) ON DELETE CASCADE
);

INSERT INTO gruppyi (Nazvanie, Shifr, DataFormir, KodPlana, Status, StatusDate) VALUES
('It-1', 1, '2014-07-23', 2, 'created', CURRENT_DATE),
('It-2', 2, '2013-09-15', 2, 'created', CURRENT_DATE),
('A-2', 3, '2013-09-15', 1, 'created', CURRENT_DATE),
('A-3', 4, '2014-09-15', 1, 'created', CURRENT_DATE);

-- В H2 2.x явные вставки не продвигают счётчик IDENTITY;
-- перезапускаем его выше сид-данных, иначе новая группа получит Shifr=1
ALTER TABLE gruppyi ALTER COLUMN Shifr RESTART WITH 5;

INSERT INTO studentyi (ShifrGruppyi, NomerZachetki, Familiya, Imya, Otchestvo, Gorod, Adres, Tel, Status, StatusDate) VALUES
(1, 10122, 'Petrov', 'Peter', 'Ivanich', 'Vitebsk', 'Moskow awe., 62, 2, 129', '+375297451421', 'graduate', CURRENT_DATE),
(1, 10123, 'Sidorov', 'Alex', 'Ivanich', 'Vitebsk', 'Moskow awe., 15, 19', '+375297751421', 'enrolled', CURRENT_DATE),
(3, 10355, 'Sharkov', 'Ted', 'Alexeevicj', 'Vitebsk', 'Moskow awe., 73, 2, 9', '+375297451471', 'academic leave', CURRENT_DATE),
(2, 10356, 'Sergeev', 'Boris', 'Ivanich', 'Minsk', 'Moskow awe., 12, 2, 19', '+375297433421', 'academic leave', CURRENT_DATE),
(3, 10361, 'Sherbakov', 'Dan', 'Sergeevich', 'Vitebsk', 'Pravdy st., 41, 19', '+375297411421', 'enrolled', CURRENT_DATE),
(4, 10368, 'Frolov', 'Dan', 'Sergeevich', 'Vitebsk', 'Stroiteley st., 41, 19', '+375297411421', 'enrolled', CURRENT_DATE),
(4, 10369, 'Firsov', 'Vlad', 'Petrovich', 'Minsk', 'Pravdy st., 41, 19', '+375297411421', 'enrolled', CURRENT_DATE),
(3, 10362, 'Ivanov', 'Andrew', 'Ivanich', 'Vitebsk', 'Pravdy st., 12, 1', '+375297451199', 'graduate', CURRENT_DATE);

CREATE TABLE IF NOT EXISTS app_user (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO app_user (username, password, role) VALUES
('user', '{noop}password', 'USER');
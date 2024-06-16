## 안드로이드 스튜디오 리듬게임 프로젝트


### 이게 뭐에요?
- **안드로이드 스튜디오** 기반 4키기반 건반 리듬게임 프로젝트입니다.
- 안드로이드 기반 리듬게임 예시가 없어서 **참고자료**가 됐으면 하는 바램으로 제작됐습니다.
- 이 소스코드를 참고하여 멋진 리듬게임을 제작하고 발표해보세요!
- 이 게임은 PC게임 **DJMAX**를 더 많은 사람이 알고 ,즐길수 있게 하고자 하는 바램으로 제작됐습니다.
- **에셋,곡** 또한 DJMAX(주 네오위즈)의 에셋을 여럿 사용했습니다.
- 이 소스코드는 비영리적인 목적을 위해 제작됐으며, **에셋을 영리적 목적으로 사용을 금**합니다.

### 구성?
- 4키 세로형 건반리듬게임 입니다. 각 레인에 터치하여 노트를 처리합니다.
- 판정선을 기준으로 노트 판정을 처리합니다. **(PERFECT/GREAT/GOOD/BAD/MISS)**
- 각 판정은 Score을 **1000/500/250/0/-500** 을 증가시키고, BAD와 MISS판정은 체력을 감소시킵니다.
- Video를 삽입하여 게임화면과 BGA를 같이 출력합니다.
- 게임이 종료되면 결과창에서 게임결과를 출력합니다.
- 최고기록을 DB(Firestore)내 최고기록과 비교하여 추월시 업데이트합니다.
- 각 곡별로 Ranking Page를 통해 자신의 최고기록과 타 유저의 최고기록을 비교합니다.
- 유저별 설정으로 **배경볼륨/게임볼륨/싱크조절/영상ON.OFF**가 있습니다.
- 게임설정으로 **배속모드/게임모드(MIRROR,RANDOM)/오토모드**가 있습니다.


### 채보시스템?

- **Osu!** 게임의 **osu!mania**의 채보제작 시스템을 이용해 채보를 제작합니다.
- export 하여 추출하면, **xxxx.osu**형식의 채보파일이 나옵니다.
- xxxx.osu를 **파싱**하여 채보데이터로 사용합니다.(최대4키)

## PREVIEW

![로그인](https://github.com/LeesuDEV/Rhythm_Max/assets/166359089/72ffb999-a139-4d48-851f-6df0673d2d92)
![곡선택](https://github.com/LeesuDEV/Rhythm_Max/assets/166359089/7d1e64c3-5007-4a02-a209-f27ac8212c35)
![세팅](https://github.com/LeesuDEV/Rhythm_Max/assets/166359089/9c3fd33f-9de3-4f0d-b670-0bbd125c0caa)
![랭킹](https://github.com/LeesuDEV/Rhythm_Max/assets/166359089/efcf723b-0d69-4c45-b814-58d95a5dcf0a)
![게임](https://github.com/LeesuDEV/Rhythm_Max/assets/166359089/62d7db0e-5782-458e-a030-1eecf4dca1b0)
![결과](https://github.com/LeesuDEV/Rhythm_Max/assets/166359089/263ebdb4-95d6-4de1-a0e2-a6b3644e4ef6)

## YOUTUBE

- **PLAY**

[![Video Label](https://github.com/LeesuDEV/Rhythm_Max/assets/166359089/239f78be-4b35-43cd-a457-59ee6ae3f689)](https://youtu.be/E8Vn29KdoV0&t=4s)
https://www.youtube.com/watch?v=E8Vn29KdoV0&t=4s

- **발표**

[![Video Label](https://github.com/LeesuDEV/Rhythm_Max/assets/166359089/d5f7b2e2-ef5a-421e-83c4-a877bc7f837b)](https://youtu.be/RuKsslMjMxg&t=604s)
https://www.youtube.com/watch?v=RuKsslMjMxg&t=604s

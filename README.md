# MusicPlayer

음악플레이어입니다.

간단한 구조
1. MusicFragment에서 Content Provider를 통해 MusicListAdapter의 ArrayList(playList)에 add.
2. add한 아이템들을 MusicFragment의 RecyclerView를 통해 보여줌.
3. MusicFragment는 MainActivity에서 보여짐.
4. RecyclerView의 아이템 클릭시 playList와 포지션을 MusicPlayService로 전달.
5. MusicPlayService에서 MediaPlayer를 통해 음악 재생.
6. MusicPlayService의 setOnPreparedListener에서 sendBroadcast를 통해 musicData와 현재 재생중인지 isPlaying(boolean) 여부를 MainActivity로 전달.
7. MainActivity에서 musicData로 MainActivity의 View들의 text와 ImageUri를 변경.
8. MainActivity의 play, next, pre 버튼 클릭시 sendBroadcast를 통해 음악 일시정지, 재생, 다음곡 재생, 이전곡 재생 조작.
9. 마찬가지로 SeekBar 조작시 SeekBar의 Progress를 MusicPlayService로 전달 후 MediaPlayer의 seekTo메소드를 통해 지정한 시간으로 노래 재생.
10. MusicPlayService의 BroadcastReceiver에서 isPlaying(현재 재생중인지)를 MainActivity로 전달(sendBroadcast).
11. isPlaying에 따라 Play Button의 이미지를 pause나 play arrow로 변경.
12. 서비스 바인딩을 통해 MusicPlayService의 getMusicCurrentTime메소드를 MainActivity에서 호출 후 SeekBar Progress에 적용.

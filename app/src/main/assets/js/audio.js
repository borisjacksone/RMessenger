document.addEventListener("DOMContentLoaded", function() { startPlayer(); }, false);
var player;
function startPlayer()
{
    player = document.getElementById('music_player');
    player.controls = false;

    player.addEventListener('loadstart', function() {
        document.getElementById('loading_status').style.display = 'inline';
        document.getElementById('duration').style.display = 'none';
        document.getElementById('duration_label').style.display = 'none';
        document.getElementById("play_button").style.display = 'none';
        document.getElementById("pause_button").style.display = 'none';
    });

    player.addEventListener('canplaythrough', function() {
        var dur = Math.floor(player.duration);
        var str = (dur < 10 ? '0' + dur : dur);
        document.getElementById('duration').innerHTML = str + 's';

        document.getElementById('loading_status').style.display = 'none';
        document.getElementById('duration').style.display = 'inline';
        document.getElementById('duration_label').style.display = 'inline';
        document.getElementById("play_button").style.display = 'inline';
        document.getElementById("pause_button").style.display = 'none';
    }, false);

    player.addEventListener('ended', function() {
        player.currentTime = 0;
        document.getElementById("play_button").style.display = 'inline';
        document.getElementById("pause_button").style.display = 'none';
    });

    player.addEventListener('playing', function() {
        document.getElementById("play_button").style.display = 'none';
        document.getElementById("pause_button").style.display = 'inline';
    });
}

function play_aud()
{
    player.play();
    document.getElementById("play_button").style.display = 'none';
    document.getElementById("pause_button").style.display = 'inline';
}

function pause_aud()
{
    player.pause();
    document.getElementById("play_button").style.display = 'inline';
    document.getElementById("pause_button").style.display = 'none';
}
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
        var str = dur < 10 ? '0' + dur : dur;
        document.getElementById('duration').innerHTML = str + 's';

        document.getElementById('loading_status').style.display = 'none';
        document.getElementById('duration').style.display = 'inline';
        document.getElementById('duration_label').style.display = 'inline';
        document.getElementById("play_button").style.display = 'inline';
        document.getElementById("pause_button").style.display = 'inline';
    }, false);
}

function play_aud()
{
    player.play();
}

function pause_aud()
{
    player.pause();
}
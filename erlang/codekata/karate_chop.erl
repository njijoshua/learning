%
% Code Kata Two - Karate Chop
% http://codekata.pragprog.com/2007/01/kata_two_karate.html
%
% Chad Gibbons
% dcgibons@gmail.com
% August 17, 2012
%

-module(karate_chop).
-export([chop/2, chop2/2]).
-import(lists, [split/2, nth/2]).
-include_lib("eunit/include/eunit.hrl").

chop_test() ->
  ?assert(-1 =:= chop(3, [])),
  ?assert(-1 =:= chop(3, [1])),
  ?assert(0 =:= chop(1, [1])),
  ?assert(0 =:= chop(1, [1, 3, 5])),
  ?assert(1 =:= chop(3, [1, 3, 5])),
  ?assert(2 =:= chop(5, [1, 3, 5])),
  ?assert(-1 =:= chop(0, [1, 3, 5])),
  ?assert(-1 =:= chop(2, [1, 3, 5])),
  ?assert(-1 =:= chop(4, [1, 3, 5])),
  ?assert(-1 =:= chop(6, [1, 3, 5])),
  ?assert(0 =:= chop(1, [1, 3, 5, 7])),
  ?assert(1 =:= chop(3, [1, 3, 5, 7])),
  ?assert(2 =:= chop(5, [1, 3, 5, 7])),
  ?assert(3 =:= chop(7, [1, 3, 5, 7])),
  ?assert(-1 =:= chop(0, [1, 3, 5, 7])),
  ?assert(-1 =:= chop(2, [1, 3, 5, 7])),
  ?assert(-1 =:= chop(4, [1, 3, 5, 7])),
  ?assert(-1 =:= chop(6, [1, 3, 5, 7])),
  ?assert(-1 =:= chop(8, [1, 3, 5, 7])).

%
% First implemenation: straight-forward recursive implementation. Not sure how 
% functional this one really is. lists:nth and dealing with artificial indicies 
% into an Erlang list seems expensive, without yet knowing how they are 
% implemented.
%
chop(Key, List) ->
  chop(Key, List, 1, length(List)).

chop(Key, List, Lower, Upper) ->
  if
    Upper < Lower ->
      -1;
    true ->
      Mid = (Lower + Upper) div 2,
      Item = nth(Mid, List),
      if
        Key < Item ->
          chop(Key, List, Lower, Mid-1);
        Key > Item ->
            chop(Key, List, Mid+1, Upper);
        true ->
          Mid-1 % lists:nth is 1 based
      end
  end.

chop2_test() ->
  ?assert(-1 =:= chop2(3, [])),
  ?assert(-1 =:= chop2(3, [1])),
  ?assert(0 =:= chop2(1, [1])),
  ?assert(0 =:= chop2(1, [1, 3, 5])),
  ?assert(1 =:= chop2(3, [1, 3, 5])),
  ?assert(2 =:= chop2(5, [1, 3, 5])),
  ?assert(-1 =:= chop2(0, [1, 3, 5])),
  ?assert(-1 =:= chop2(2, [1, 3, 5])),
  ?assert(-1 =:= chop2(4, [1, 3, 5])),
  ?assert(-1 =:= chop2(6, [1, 3, 5])),
  ?assert(0 =:= chop2(1, [1, 3, 5, 7])),
  ?assert(1 =:= chop2(3, [1, 3, 5, 7])),
  ?assert(2 =:= chop2(5, [1, 3, 5, 7])),
  ?assert(3 =:= chop2(7, [1, 3, 5, 7])),
  ?assert(-1 =:= chop2(0, [1, 3, 5, 7])),
  ?assert(-1 =:= chop2(2, [1, 3, 5, 7])),
  ?assert(-1 =:= chop2(4, [1, 3, 5, 7])),
  ?assert(-1 =:= chop2(6, [1, 3, 5, 7])),
  ?assert(-1 =:= chop2(8, [1, 3, 5, 7])).

%
% Second implementation: same as the first, except take advantage of Erlang 
% guard when determining if the search hasn't yet completed. Same caveats as 
% previous implementation.
%
chop2(Key, List) ->
  chop2(Key, List, 1, length(List)).

chop2(_, _, Lower, Upper) when Upper < Lower ->
  -1;
chop2(Key, List, Lower, Upper) ->
  Mid = (Lower + Upper) div 2,
  Item = nth(Mid, List),
  if
    Key < Item ->
      chop2(Key, List, Lower, Mid-1);
    Key > Item ->
      chop(Key, List, Mid+1, Upper);
    true ->
      Mid-1 % lists:nth is 1 based
  end.


%
% Third implementation: array slices with an accumulator! This begs the 
% question, in Erlang, is splitting a list more efficient than using the 
% lists:nth() function and passing the same array through recursive calls? And 
% is either operation clearer? Perhaps to me only, not splitting the array (2nd 
% implementation) is more clear.
%
chop3(Key, List) ->
    chop3(Key, List, 0).
chop3(_, [], _) ->
    -1;
chop3(Key, List, AccIn) ->
    Mid = length(List) div 2,
    {List1, [Item|List2]} = split(Mid, List),
    if
        Key < Item ->
            chop3(Key, List1, AccIn);
        Key > Item ->
            chop3(Key, List2, AccIn + Mid + 1);
        Key =:= Item ->
            Mid + AccIn
    end.

chop3_test() ->
  ?assert(-1 =:= chop3(3, [])),
  ?assert(-1 =:= chop3(3, [1])),
  ?assert(0 =:= chop3(1, [1])),
  ?assert(0 =:= chop3(1, [1, 3, 5])),
  ?assert(1 =:= chop3(3, [1, 3, 5])),
  ?assert(2 =:= chop3(5, [1, 3, 5])),
  ?assert(-1 =:= chop3(0, [1, 3, 5])),
  ?assert(-1 =:= chop3(2, [1, 3, 5])),
  ?assert(-1 =:= chop3(4, [1, 3, 5])),
  ?assert(-1 =:= chop3(6, [1, 3, 5])),
  ?assert(0 =:= chop3(1, [1, 3, 5, 7])),
  ?assert(1 =:= chop3(3, [1, 3, 5, 7])),
  ?assert(2 =:= chop3(5, [1, 3, 5, 7])),
  ?assert(3 =:= chop3(7, [1, 3, 5, 7])),
  ?assert(-1 =:= chop3(0, [1, 3, 5, 7])),
  ?assert(-1 =:= chop3(2, [1, 3, 5, 7])),
  ?assert(-1 =:= chop3(4, [1, 3, 5, 7])),
  ?assert(-1 =:= chop3(6, [1, 3, 5, 7])),
  ?assert(-1 =:= chop3(8, [1, 3, 5, 7])).
